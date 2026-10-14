package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.mapper.aSesion
import pe.edu.upeu.pharmamobile.data.remote.AuthApi
import pe.edu.upeu.pharmamobile.data.remote.dto.LoginRequestDto
import pe.edu.upeu.pharmamobile.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesionException
import pe.edu.upeu.pharmamobile.domain.model.EstadoSesion
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository

class AuthRepositorioRest(
    private val api: AuthApi,
    private val almacen: AlmacenSeguro
) : AuthRepository {

    private val _estado = MutableStateFlow<EstadoSesion>(EstadoSesion.Verificando)
    override val estado: StateFlow<EstadoSesion> = _estado.asStateFlow()

    override suspend fun iniciarSesion(credenciales: Credenciales): Sesion {
        val dto = ejecutarLlamada {
            api.login(LoginRequestDto(credenciales.usuario.trim(), credenciales.clave))
        }.getOrElse { throw ErrorSesionException(mapearErrorSesion(it, esLogin = true)) }

        almacen.guardarToken(dto.token)
        api.olvidarTokenEnCache()            // la próxima petición relee el almacén
        return dto.aSesion().also { _estado.value = EstadoSesion.Activa(it) }
    }

    /** Al abrir la app: si hay token, /auth/me confirma que sigue vigente y trae los permisos. */
    override suspend fun sesionGuardada(): Sesion? {
        val token = almacen.leerToken()
        if (token == null) {
            _estado.value = EstadoSesion.Inactiva()
            return null
        }
        return ejecutarLlamada { api.usuarioActual() }.fold(
            onSuccess = { me -> me.aSesion(token).also { _estado.value = EstadoSesion.Activa(it) } },
            onFailure = { fallo ->
                val error = (fallo as? ErrorApiException)?.error
                if (error is ErrorApi.NoAutenticado) {
                    cerrarSesion(error.mensaje)      // expiró o lo deshabilitaron con la app cerrada
                } else {
                    // Sin red: el token se conserva. Qué hacer aquí es justo la pregunta de la sesión 11.
                    _estado.value = EstadoSesion.Inactiva("No se pudo verificar la sesión: sin conexión con el servidor")
                }
                null
            }
        )
    }

    override suspend fun cerrarSesion(aviso: String?) {
        almacen.borrarToken()          // efecto 1
        api.olvidarTokenEnCache()      // efecto 2
        _estado.value = EstadoSesion.Inactiva(aviso)   // efectos 3 y 4 (la UI reacciona)
    }
}

/** Único punto que traduce un fallo de red a la jerarquía ErrorSesion. */
internal fun mapearErrorSesion(fallo: Throwable, esLogin: Boolean = false): ErrorSesion =
    when (val error = (fallo as? ErrorApiException)?.error) {
        is ErrorApi.NoAutenticado -> when {
            esLogin -> ErrorSesion.CredencialesInvalidas
            "expirado" in error.mensaje -> ErrorSesion.SesionExpirada(error.mensaje)
            else -> ErrorSesion.NoAutenticado(error.mensaje)
        }
        is ErrorApi.SinPermiso -> ErrorSesion.SinPermiso
        else -> ErrorSesion.SinConexion
    }
