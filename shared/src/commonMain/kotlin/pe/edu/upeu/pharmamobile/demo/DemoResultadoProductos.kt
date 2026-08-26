package pe.edu.upeu.pharmamobile.demo

import pe.edu.upeu.pharmamobile.domain.result.ResultadoProductos

fun mostrarResultado(resultado: ResultadoProductos){
    when(resultado){
        ResultadoProductos.Cargando -> {
            println(
                "Cargando productos"
            )
        }
        is ResultadoProductos.Exito -> {
            println(
                "Productos encontrados: ${resultado.productos.size}"
            )
        }
        is ResultadoProductos.Error -> {
            println(
                "Error: ${resultado.mensaje}"
            )
        }
    }
}