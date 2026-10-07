package pe.edu.upeu.pharmamobile.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.platform.CompartidorAndroid

actual val platformModule = module {
    single<HttpClientEngine> { OkHttp.create() }
    // El emulador ve al PC anfitrion como 10.0.2.2, no como localhost.
    single(named(URL_BASE)) { "http://10.0.2.2:8080/api/v1/" }
    single<Compartidor> { CompartidorAndroid(androidContext()) }
}
