package pe.edu.upeu.pharmamobile.domain.usecase

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

/** Reemplaza a ListarProductosUseCase: ya no se pide la lista, se observa. */
class ObservarProductosUseCase(private val repository: ProductoRepository) {
    operator fun invoke(): Flow<List<Producto>> = repository.observar()
}

/** Subir lo pendiente y bajar del servidor. El fallo (sin red) llega como Result, no como excepción. */
class SincronizarProductosUseCase(private val repository: ProductoRepository) {
    suspend operator fun invoke(): Result<Unit> = resultadoDe { repository.sincronizar() }
}

/** Para mostrar «Actualizado hace 2 horas» (actividad autónoma, producto 3). */
class ObservarUltimaSincronizacionUseCase(private val repository: ProductoRepository) {
    operator fun invoke(): Flow<Long?> = repository.observarUltimaSincronizacion()
}
