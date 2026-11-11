package pe.edu.upeu.pharmamobile.data.mapper

import pe.edu.upeu.pharmamobile.data.remote.dto.LoginResponseDto
import pe.edu.upeu.pharmamobile.data.remote.dto.UsuarioActualDto
import pe.edu.upeu.pharmamobile.domain.model.Sesion

fun LoginResponseDto.aSesion() = Sesion(token, username, nombreCompleto, permisos.toSet())

fun UsuarioActualDto.aSesion(token: String) =
    Sesion(token, username, empleadoNombre ?: username, permisos.toSet())
