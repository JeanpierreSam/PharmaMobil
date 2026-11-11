package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.PerfilLocal
import pe.edu.upeu.pharmamobile.data.mapper.aSesion
import pe.edu.upeu.pharmamobile.data.remote.AuthApi
import pe.edu.upeu.pharmamobile.data.remote.dto.LoginRequestDto
import pe.edu.upeu.pharmamobile.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesionException
import pe.edu.upeu.pharmamobile.domain.model.EstadoSesion
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository

class AuthRepositorioRest(
    private val api: AuthApi,
    private val almacen: AlmacenSeguro,
    /** Sesión 11: perfil de la última sesión verificada; null en las pruebas de la sesión 10. */
    private val perfil: PerfilLocal? = null
) : AuthRepository {

    private val _estado = MutableStateFlow<EstadoSesion>(EstadoSesion.Verificando)
    override val estado: StateFlow<EstadoSesion> = _estado.asStateFlow()

    override suspend fun iniciarSesion(credenciales: Credenciales): Sesion {
        val dto = ejecutarLlamada {
            api.login(LoginRequestDto(credenciales.usuario.trim(), credenciales.clave))
        }.getOrElse { throw ErrorSesionException(mapearErrorSesion(it, esLogin = true)) }

        almacen.guardarToken(dto.token)
        api.olvidarTokenEnCache()            // la próxima petición relee el almacén
        return dto.aSesion().also {
            perfil?.guardar(it)
            _estado.value = EstadoSesion.Activa(it)
        }
    }

    /** Al abrir la app: si hay token, /auth/me confirma que sigue vigente y trae los permisos. */
    override suspend fun sesionGuardada(): Sesion? {
        val token = almacen.leerToken()
        if (token == null) {
            _estado.value = EstadoSesion.Inactiva()
            return null
        }
        val resultado = ejecutarLlamada { api.usuarioActual() }
        val error = (resultado.exceptionOrNull() as? ErrorApiException)?.error
        return when {
            resultado.isSuccess -> resultado.getOrThrow().aSesion(token).also {
                perfil?.guardar(it)                  // permisos al día para la próxima vez sin red
                _estado.value = EstadoSesion.Activa(it)
            }
            error is ErrorApi.NoAutenticado -> {
                cerrarSesion(error.mensaje)          // expiró o lo deshabilitaron con la app cerrada
                null
            }
            else -> {
                // Sesión 11: sin red no se puede verificar el token, pero tampoco hay
                // motivo para echar al usuario. Entra con el perfil guardado y ve sus
                // datos locales; si el token ya no vale, el primer 401 al reconectar
                // cierra la sesión (alPerderSesion de ProductoRepositorioRest).
                val guardada = perfil?.leer(token)
                _estado.value = if (guardada != null) {
                    EstadoSesion.Activa(guardada)
                } else {
                    EstadoSesion.Inactiva("No se pudo verificar la sesión: sin conexión con el servidor")
                }
                guardada
            }
        }
    }

    override suspend fun cerrarSesion(aviso: String?) {
        almacen.borrarToken()          // efecto 1
        perfil?.borrar()               // sin sesión no queda perfil para entrar sin red
        api.olvidarTokenEnCache()      // efecto 2
        _estado.value = EstadoSesion.Inactiva(aviso)   // efectos 3 y 4 (la UI reacciona)
    }
}

/** Único punto que traduce un fallo de red a la jerarquía ErrorSesion. */
internal fun mapearErrorSesion(fallo: Throwable, esLogin: Boolean = false): ErrorSesion =
    when (val error = (fallo as? ErrorApiException)?.error) {
        is ErrorApi.NoAutenticado -> when {
            esLogin -> ErrorSesion.CredencialesInvalidas
            "expirado" in error.mensaje -> ErrorSesion.SesionExpirada(error.mensaje)
            else -> ErrorSesion.NoAutenticado(error.mensaje)
        }
        is ErrorApi.SinPermiso -> ErrorSesion.SinPermiso
        else -> ErrorSesion.SinConexion
    }
