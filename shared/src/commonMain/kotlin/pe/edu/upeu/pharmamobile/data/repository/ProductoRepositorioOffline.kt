package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.EstadoEnvio
import pe.edu.upeu.pharmamobile.domain.model.MAX_INTENTOS_ENVIO
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.ResultadoEnvio
import pe.edu.upeu.pharmamobile.domain.model.ResumenSync
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.platform.ahoraEnMilis

/**
 * Offline-first: la base local es la fuente de verdad y [remoto] (PharmaSoft)
 * solo la mantiene al día. Se reutiliza ProductoRepositorioRest como remoto
 * porque ya traduce los fallos de Ktor a ErrorApi y cierra la sesión ante un 401.
 *
 * Sesión 13: cada fila recuerda sus intentos y su último error. Lo transitorio
 * se reintenta con una espera que crece; lo permanente se marca y se muestra.
 */
class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val remoto: ProductoRepositorioRest,
    /** Se inyecta para que las pruebas usen una hora fija. */
    private val reloj: () -> Long = ::ahoraEnMilis,
    /** Se inyecta para que las pruebas registren las esperas sin esperar de verdad. */
    private val esperar: suspend (Long) -> Unit = { delay(it) }
) : ProductoRepository {

    override fun observar(): Flow<List<Producto>> = local.observar()

    override fun observarUltimaSincronizacion(): Flow<Long?> = local.observarUltimaSincronizacion()

    override fun observarEstadoEnvio(): Flow<EstadoEnvio> =
        combine(
            local.observarPorEnviar(MAX_INTENTOS_ENVIO),
            local.observarFallidas(MAX_INTENTOS_ENVIO)
        ) { pendientes, fallidas -> EstadoEnvio(pendientes, fallidas) }

    override suspend fun sincronizar(): ResumenSync {
        var enviadas = 0
        var fallidas = 0

        // 1.º subir lo que el usuario hizo sin red, fila por fila
        for (fila in local.porEnviar(MAX_INTENTOS_ENVIO)) {
            when (val resultado = intentarEnvio(fila.producto)) {
                is ResultadoEnvio.Exito -> enviadas++
                is ResultadoEnvio.Permanente -> {
                    local.marcarFallida(fila.producto.id, resultado.motivo, MAX_INTENTOS_ENVIO)
                    fallidas++
                }
                is ResultadoEnvio.Transitorio -> {
                    local.sumarIntento(fila.producto.id, resultado.motivo)
                    if (fila.intentos + 1 >= MAX_INTENTOS_ENVIO) {
                        fallidas++                                  // se rindió: queda a la vista
                    } else {
                        esperar(esperaParaIntento(fila.intentos))   // 1 s, 2 s, 4 s, 8 s
                    }
                }
            }
        }

        // 2.º la verdad del servidor. Solo después de subir: invertirlo borra lo pendiente.
        local.reemplazarSincronizados(remoto.listar())
        local.registrarSincronizacion(reloj())              // solo si las dos anteriores funcionaron
        return ResumenSync(enviadas, fallidas, local.contarPorEnviar(MAX_INTENTOS_ENVIO))
    }

    override suspend fun reintentarFallidos() {
        local.reiniciarIntentos()
    }

    override suspend fun descartarPendiente(id: Long) {
        require(id < 0) { "Solo se descarta lo que nunca llegó a PharmaSoft" }
        local.borrar(id)
    }

    override suspend fun cambiosSinEnviar(): Int = local.pendientes().size

    override suspend fun borrarDatosLocales() {
        local.borrarTodo()
    }

    /** El formulario de edición y el detalle leen de la base: funcionan sin red. */
    override suspend fun obtener(id: Long): Producto =
        local.obtener(id) ?: throw ErrorApiException(ErrorApi.NoEncontrado)

    override suspend fun registrar(producto: Producto): Producto {
        val pendiente = local.guardarPendiente(producto)    // la pantalla ya lo ve
        return try {
            enviar(pendiente)
        } catch (e: ErrorApiException) {
            if (!e.error.dejaPendiente()) {
                // Con red, PharmaSoft lo rechazó (400, 409, 403): no se guarda y el
                // formulario muestra el error bajo su campo, como en la sesión 8.
                local.borrar(pendiente.id)
                throw e
            }
            pendiente                                       // sin red: se enviará al sincronizar
        }
    }

    override suspend fun actualizar(producto: Producto): Producto {
        if (producto.id < 0) {                              // aún no existe en PharmaSoft
            local.actualizarPendiente(producto)             // y vuelve a la cola con intentos = 0
            return producto
        }
        return remoto.actualizar(producto).also { local.guardarSincronizado(it) }
    }

    override suspend fun eliminar(id: Long) {
        if (id < 0) {                                       // nunca llegó al servidor: basta con quitarlo
            local.borrar(id)
            return
        }
        remoto.eliminar(id)                                 // baja lógica en PharmaSoft
        local.obtener(id)?.let { local.guardarSincronizado(it.copy(activo = false)) }
    }

    /**
     * Un intento de envío. Sin red o sin sesión NO es un intento: el servidor no
     * llegó a juzgar el dato. Se corta toda la sincronización y se reintenta luego.
     */
    private suspend fun intentarEnvio(pendiente: Producto): ResultadoEnvio =
        try {
            ResultadoEnvio.Exito(enviar(pendiente).id)
        } catch (e: ErrorApiException) {
            if (e.error.cortaLaSincronizacion()) throw e
            e.error.aResultado()
        }

    /** POST y, si responde, la fila local (id negativo) pasa a ser la del servidor. */
    private suspend fun enviar(pendiente: Producto): Producto =
        remoto.registrar(pendiente).also { local.confirmarSubida(pendiente.id, it) }

    /** Fallos de conectividad o de sesión: el dato es válido, solo hay que esperar. */
    private fun ErrorApi.dejaPendiente(): Boolean = when (this) {
        ErrorApi.SinConexion, ErrorApi.TiempoAgotado, ErrorApi.Servidor, is ErrorApi.NoAutenticado -> true
        is ErrorApi.Validacion, is ErrorApi.SinPermiso, ErrorApi.NoEncontrado, is ErrorApi.Conflicto -> false
    }

    private fun ErrorApi.cortaLaSincronizacion(): Boolean =
        this == ErrorApi.SinConexion || this is ErrorApi.NoAutenticado
}

/**
 * Traducción de cada error de la API a una de las tres ramas. Vive en la capa de
 * datos, como la traducción de errores de la sesión 8. `when` exhaustivo: si mañana
 * agregas un caso a ErrorApi, el compilador te obliga a clasificarlo.
 */
internal fun ErrorApi.aResultado(): ResultadoEnvio = when (this) {
    ErrorApi.SinConexion, ErrorApi.TiempoAgotado, ErrorApi.Servidor ->
        ResultadoEnvio.Transitorio(motivo())
    is ErrorApi.Validacion, ErrorApi.NoEncontrado, is ErrorApi.Conflicto, is ErrorApi.SinPermiso ->
        ResultadoEnvio.Permanente(motivo())
    // La guía lo trata como permanente. Aquí el dato es válido y solo falta volver a
    // iniciar sesión; además nunca llega a esta rama porque corta la sincronización.
    is ErrorApi.NoAutenticado -> ResultadoEnvio.Transitorio(motivo())
}

/** Texto técnico corto que se guarda en ultimoError y ve el usuario. Sin datos personales. */
internal fun ErrorApi.motivo(): String = when (this) {
    ErrorApi.SinConexion -> "Sin conexión"
    ErrorApi.TiempoAgotado -> "Tiempo agotado"
    ErrorApi.Servidor -> "Servidor no disponible (5xx)"
    is ErrorApi.NoAutenticado -> "401 · Sesión vencida"
    is ErrorApi.Validacion ->
        "400 · " + porCampo.entries.joinToString { "${it.key}: ${it.value}" }.ifBlank { "Datos no válidos" }
    is ErrorApi.SinPermiso -> "403 · $mensaje"
    ErrorApi.NoEncontrado -> "404 · Recurso no encontrado"
    is ErrorApi.Conflicto -> "409 · $mensaje"
}

/** 1 s, 2 s, 4 s, 8 s, 16 s… con tope: una red intermitente no castiga la batería. */
internal fun esperaParaIntento(intentos: Int): Long = 1_000L shl intentos.coerceIn(0, 4)
