package pe.edu.upeu.pharmamobile.domain.query

import pe.edu.upeu.pharmamobile.domain.model.Producto

// Paso 4 de la guía práctica: filter / map / find. Funciones puras, no mutan la lista.
fun List<Producto>.disponibles(): List<Producto> = filter { it.stock > 0 }

fun List<Producto>.nombres(): List<String> = map { it.nombre }

fun List<Producto>.buscarPorId(id: Long): Producto? = find { it.id == id }

fun List<Producto>.valorTotalInventario(): Double = sumOf { it.valorInventario() }

fun List<Producto>.masCaro(): Producto? = maxByOrNull { it.precio }

fun List<Producto>.conStockBajo(minimo: Int = 25): List<Producto> = filter { it.stock < minimo }
