package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class IniciarSesionUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke(credenciales: Credenciales): Result<Sesion> =
        resultadoDe { repositorio.iniciarSesion(credenciales) }
}

/**
 * Cierre VOLUNTARIO: primero se limpia la base y después se cierra la sesión.
 * El 401 no pasa por aquí (lo maneja alPerderSesion) y por eso conserva lo pendiente.
 */
class CerrarSesionUseCase(
    private val repositorio: AuthRepository,
    private val productos: ProductoRepository
) {
    suspend operator fun invoke() {
        productos.borrarDatosLocales()
        repositorio.cerrarSesion()
    }
}

class ContarCambiosSinEnviarUseCase(private val productos: ProductoRepository) {
    suspend operator fun invoke(): Int = productos.cambiosSinEnviar()
}

class RestaurarSesionUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke(): Sesion? = repositorio.sesionGuardada()
}
