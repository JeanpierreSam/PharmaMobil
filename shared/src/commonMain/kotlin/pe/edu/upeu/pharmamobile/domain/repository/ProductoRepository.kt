package pe.edu.upeu.pharmamobile.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobile.domain.model.Producto

/**
 * Sesión 11: el repositorio deja de devolver una foto (listar) y ofrece una
 * suscripción. La pantalla observa la base local; la red solo la mantiene al día.
 */
interface ProductoRepository {

    /** La pantalla se suscribe una vez y se actualiza sola. */
    fun observar(): Flow<List<Producto>>

    /** Sube lo pendiente y luego trae la lista del servidor. En ese orden. */
    suspend fun sincronizar()

    suspend fun obtener(id: Long): Producto

    /** Guarda en local; intenta enviar si hay conexión. Sin red devuelve el producto con id negativo. */
    suspend fun registrar(producto: Producto): Producto

    suspend fun actualizar(producto: Producto): Producto

    suspend fun eliminar(id: Long)
}
