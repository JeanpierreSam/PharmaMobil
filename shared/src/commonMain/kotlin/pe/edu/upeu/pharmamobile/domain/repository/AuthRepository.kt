package pe.edu.upeu.pharmamobile.domain.repository

import kotlinx.coroutines.flow.StateFlow
import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.EstadoSesion
import pe.edu.upeu.pharmamobile.domain.model.Sesion

interface AuthRepository {
    val estado: StateFlow<EstadoSesion>
    suspend fun iniciarSesion(credenciales: Credenciales): Sesion
    suspend fun sesionGuardada(): Sesion?
    suspend fun cerrarSesion(aviso: String? = null)
}
