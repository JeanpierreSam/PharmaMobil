package pe.edu.upeu.pharmamobile.di

import android.content.Context
import android.content.pm.ApplicationInfo
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguroAndroid
import pe.edu.upeu.pharmamobile.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobile.data.local.CreadorDeDriverAndroid
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.domain.platform.Notificador
import pe.edu.upeu.pharmamobile.permiso.GestorPermisosAndroid
import pe.edu.upeu.pharmamobile.platform.CompartidorAndroid
import pe.edu.upeu.pharmamobile.platform.NotificadorAndroid

actual val platformModule = module {
    single<HttpClientEngine> { OkHttp.create() }
    // El emulador ve al PC anfitrion como 10.0.2.2, no como localhost.
    single(named(URL_BASE)) { "http://10.0.2.2:8080/api/v1/" }
    single<Compartidor> { CompartidorAndroid(androidContext()) }
    single<AlmacenSeguro> { AlmacenSeguroAndroid(androidContext()) }
    single<CreadorDeDriver> { CreadorDeDriverAndroid(androidContext()) }
    // Sesión 13: la misma instancia para la Activity (puente) y para el dominio (interfaz).
    single {
        GestorPermisosAndroid(
            androidContext(),
            androidContext().getSharedPreferences("permisos", Context.MODE_PRIVATE)
        )
    }
    single<GestorPermisos> { get<GestorPermisosAndroid>() }
    single<Notificador> { NotificadorAndroid(androidContext()) }
    single(named(ES_DEPURACION)) {
        (androidContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
}
