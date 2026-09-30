package pe.edu.upeu.pharmamobile.domain.repository

import pe.edu.upeu.pharmamobile.domain.model.Producto

interface ProductoRepository {
    suspend fun registrar(nombre: String, precio: Double, stock: Int, activo: Boolean): Producto
    suspend fun listar(): List<Producto>
}
