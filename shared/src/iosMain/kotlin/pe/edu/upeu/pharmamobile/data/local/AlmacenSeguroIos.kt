package pe.edu.upeu.pharmamobile.data.local

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.posix.memcpy

/** Elemento del Keychain: servicio «pe.edu.upeu.pharmamobile», cuenta «jwt». */
@OptIn(ExperimentalForeignApi::class)
class AlmacenSeguroIos : AlmacenSeguro {

    override fun guardarToken(token: String) {
        borrarToken()
        val datos = CFBridgingRetain(token.encodeToByteArray().toNSData())
        try {
            conConsulta(
                kSecValueData to datos,
                kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
            ) { SecItemAdd(it, null) }
        } finally {
            CFRelease(datos)
        }
    }

    override fun leerToken(): String? = memScoped {
        val resultado = alloc<CFTypeRefVar>()
        val estado = conConsulta(
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne
        ) { SecItemCopyMatching(it, resultado.ptr) }
        if (estado != errSecSuccess) return@memScoped null
        (CFBridgingRelease(resultado.value) as? NSData)?.toByteArray()?.decodeToString()
    }

    override fun borrarToken() {
        conConsulta { SecItemDelete(it) }
    }

    private inline fun <T> conConsulta(
        vararg extras: Pair<CFStringRef?, CFTypeRef?>,
        accion: (CFDictionaryRef?) -> T
    ): T {
        val servicio = CFBridgingRetain(SERVICIO)
        val cuenta = CFBridgingRetain(CUENTA)
        val consulta = CFDictionaryCreateMutable(
            null, 0, kCFTypeDictionaryKeyCallBacks.ptr, kCFTypeDictionaryValueCallBacks.ptr
        )
        CFDictionaryAddValue(consulta, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(consulta, kSecAttrService, servicio)
        CFDictionaryAddValue(consulta, kSecAttrAccount, cuenta)
        extras.forEach { (clave, valor) -> CFDictionaryAddValue(consulta, clave, valor) }
        return try {
            accion(consulta)
        } finally {
            CFRelease(consulta); CFRelease(servicio); CFRelease(cuenta)
        }
    }

    private companion object {
        const val SERVICIO = "pe.edu.upeu.pharmamobile"
        const val CUENTA = "jwt"
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = usePinned {
    NSData.create(bytes = it.addressOf(0), length = size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).also { destino ->
    if (destino.isNotEmpty()) destino.usePinned { memcpy(it.addressOf(0), bytes, length) }
}
