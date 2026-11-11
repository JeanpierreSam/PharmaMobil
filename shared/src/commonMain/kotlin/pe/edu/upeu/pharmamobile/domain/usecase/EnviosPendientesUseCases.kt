package pe.edu.upeu.pharmamobile.domain.usecase

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobile.domain.model.EstadoEnvio
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

/** Sesión 13: lo que la barra muestra sin abrir ningún menú. */
class ObservarEstadoEnvioUseCase(private val repository: ProductoRepository) {
    operator fun invoke(): Flow<EstadoEnvio> = repository.observarEstadoEnvio()
}

class ReintentarEnviosFallidosUseCase(private val repository: ProductoRepository) {
    suspend operator fun invoke(): Result<Unit> = resultadoDe { repository.reintentarFallidos() }
}

class DescartarEnvioUseCase(private val repository: ProductoRepository) {
    suspend operator fun invoke(id: Long): Result<Unit> = resultadoDe { repository.descartarPendiente(id) }
}
