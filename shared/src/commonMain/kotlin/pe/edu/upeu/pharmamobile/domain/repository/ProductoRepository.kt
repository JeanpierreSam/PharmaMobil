package pe.edu.upeu.pharmamobile.domain.repository

import pe.edu.upeu.pharmamobile.domain.model.Producto

interface ProductoRepository {
    suspend fun listar(): List<Producto>
    suspend fun obtener(id: Long): Producto
    /** Recibe el producto con id 0; devuelve el que asignó el almacenamiento. */
    suspend fun registrar(producto: Producto): Producto
    suspend fun actualizar(producto: Producto): Producto
    suspend fun eliminar(id: Long)
}
