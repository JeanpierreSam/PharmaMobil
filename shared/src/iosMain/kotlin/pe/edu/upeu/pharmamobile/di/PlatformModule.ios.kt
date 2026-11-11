@file:OptIn(kotlin.experimental.ExperimentalNativeApi::class)

package pe.edu.upeu.pharmamobile.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguroIos
import pe.edu.upeu.pharmamobile.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobile.data.local.CreadorDeDriverIos
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.domain.platform.MonitorRed
import pe.edu.upeu.pharmamobile.domain.platform.Notificador
import pe.edu.upeu.pharmamobile.platform.MonitorRedIos
import pe.edu.upeu.pharmamobile.permiso.GestorPermisosIos
import pe.edu.upeu.pharmamobile.platform.CompartidorIos
import pe.edu.upeu.pharmamobile.platform.NotificadorIos

actual val platformModule = module {
    single<HttpClientEngine> { Darwin.create() }
    // El simulador de iOS comparte la red del Mac: localhost llega al backend.
    single(named(URL_BASE)) { "http://localhost:8080/api/v1/" }
    single<Compartidor> { CompartidorIos() }
    single<AlmacenSeguro> { AlmacenSeguroIos() }
    single<CreadorDeDriver> { CreadorDeDriverIos() }
    single<GestorPermisos> { GestorPermisosIos() }
    single<Notificador> { NotificadorIos() }
    single<MonitorRed> { MonitorRedIos() }
    // Nombre completo: el proyecto ya tiene su propia interfaz Platform (plantilla KMP).
    single(named(ES_DEPURACION)) { kotlin.native.Platform.isDebugBinary }
}
