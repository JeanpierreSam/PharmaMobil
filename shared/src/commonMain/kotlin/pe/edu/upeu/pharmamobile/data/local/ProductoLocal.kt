package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobile.db.PharmaDb
import pe.edu.upeu.pharmamobile.domain.model.Producto
// SQLDelight genera una clase «Producto» (la fila de la tabla) que choca de
// nombre con la del dominio: se importa con alias.
import pe.edu.upeu.pharmamobile.db.Producto as ProductoEntity

/**
 * Concentra las consultas de la tabla producto y traduce entre filas y dominio.
 * No conoce Ktor ni los DTO: lo que venga del servidor llega ya como Producto.
 */
class ProductoLocal(base: PharmaDb) {

    private val consultas = base.productoQueries
    private val sincronizacion = base.sincronizacionQueries

    /** Emite de nuevo cada vez que la tabla cambia. */
    fun observar(): Flow<List<Producto>> =
        consultas.listarTodos()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { filas -> filas.map { it.aDominio() } }

    fun pendientes(): List<Producto> =
        consultas.listarPendientes().executeAsList().map { it.aDominio() }

    fun obtener(id: Long): Producto? =
        consultas.obtenerPorId(id).executeAsOneOrNull()?.aDominio()

    /** Lo que el usuario crea: id local negativo y marca de pendiente. */
    fun guardarPendiente(producto: Producto): Producto =
        consultas.transactionWithResult {
            val menor = consultas.siguienteIdLocal().executeAsOne()
            val local = producto.copy(id = minOf(menor, 0L) - 1L)
            escribir(local, pendiente = true)
            local
        }

    /** Edición de un producto que todavía no llegó al servidor (id negativo). */
    fun actualizarPendiente(producto: Producto) = escribir(producto, pendiente = true)

    /** Producto que el servidor ya confirmó (actualización o baja con red). */
    fun guardarSincronizado(producto: Producto) = escribir(producto, pendiente = false)

    /** El POST funcionó: la fila local (id negativo) se cambia por la del servidor (id real). */
    fun confirmarSubida(idLocal: Long, delServidor: Producto) {
        consultas.transaction {
            consultas.borrarPorId(idLocal)
            escribir(delServidor, pendiente = false)
        }
    }

    fun borrar(id: Long) {
        consultas.borrarPorId(id)
    }

    /**
     * Recibe la lista del servidor. Borra SOLO lo sincronizado: lo pendiente
     * se conserva. Va en una transacción para que el Flow emita una sola vez.
     */
    fun reemplazarSincronizados(productos: List<Producto>) {
        consultas.transaction {
            consultas.borrarSincronizados()
            productos.forEach { escribir(it, pendiente = false) }
        }
    }

    // --- Antigüedad de los datos (actividad autónoma, producto 3) ----------

    /** Instante de la última sincronización correcta; null si nunca hubo una. */
    fun observarUltimaSincronizacion(): Flow<Long?> =
        sincronizacion.leerUltima()
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)

    fun registrarSincronizacion(instante: Long) {
        sincronizacion.guardarUltima(instante)
    }

    private fun escribir(p: Producto, pendiente: Boolean) {
        consultas.insertar(
            id = p.id,
            nombre = p.nombre,
            precio = p.precio,
            stock = p.stock.toLong(),
            activo = if (p.activo) 1L else 0L,
            pendiente = if (pendiente) 1L else 0L
        )
    }
}

private fun ProductoEntity.aDominio() = Producto(
    id = id,
    nombre = nombre,
    precio = precio,
    stock = stock.toInt(),
    activo = activo == 1L
)
