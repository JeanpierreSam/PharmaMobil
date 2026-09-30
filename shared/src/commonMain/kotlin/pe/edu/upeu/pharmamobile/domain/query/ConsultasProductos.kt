package pe.edu.upeu.pharmamobile.domain.query

import pe.edu.upeu.pharmamobile.domain.model.Producto

// Paso 4 de la guía práctica: filter / map / find. Funciones puras, no mutan la lista.
fun List<Producto>.disponibles(): List<Producto> = filter { it.stock > 0 }

fun List<Producto>.nombres(): List<String> = map { it.nombre }

fun List<Producto>.buscarPorId(id: Long): Producto? = find { it.id == id }

fun List<Producto>.valorTotalInventario(): Double = sumOf { it.valorInventario() }

fun List<Producto>.masCaro(): Producto? = maxByOrNull { it.precio }

// Clasificación de la pestaña Activos: activo == true.
fun List<Producto>.activos(): List<Producto> = filter { it.activo }

// Clasificación de la pestaña Inactivos: activo == false.
fun List<Producto>.inactivos(): List<Producto> = filter { !it.activo }

/**
 * Clasificación transversal de Bajo Stock (Sesión 4, Reto 02): puede coincidir con
 * Activos o Inactivos. El umbral vive en [Producto.requiereReposicion] para que exista
 * una única fuente de verdad (Sesión 5, Reto 01).
 */
fun List<Producto>.bajoStock(): List<Producto> = filter { it.requiereReposicion }
