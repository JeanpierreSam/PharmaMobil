package pe.edu.upeu.pharmamobile.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule = module {
    single<HttpClientEngine> { Darwin.create() }
    // El simulador de iOS comparte la red del Mac: localhost llega al backend.
    single(named(URL_BASE)) { "http://localhost:8080/api/v1/" }
}
