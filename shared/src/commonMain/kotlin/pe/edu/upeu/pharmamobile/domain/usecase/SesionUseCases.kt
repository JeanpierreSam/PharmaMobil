package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository

class IniciarSesionUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke(credenciales: Credenciales): Result<Sesion> =
        resultadoDe { repositorio.iniciarSesion(credenciales) }
}

class CerrarSesionUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke() = repositorio.cerrarSesion()
}

class RestaurarSesionUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke(): Sesion? = repositorio.sesionGuardada()
}
