package pe.edu.upeu.pharmamobile.di

import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.remote.AuthApi
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobile.data.repository.AuthRepositorioRest
import pe.edu.upeu.pharmamobile.data.repository.ProductoRepositorioRest
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RestaurarSesionUseCase
import pe.edu.upeu.pharmamobile.presentation.detalle.DetalleProductoViewModel
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobile.presentation.sesion.SesionViewModel

val dataModule = module {
    single { crearHttpClient(get(), get(named(URL_BASE)), get<AlmacenSeguro>(), get(named(ES_DEPURACION))) }
    single { ProductoApi(get()) }
    single { AuthApi(get()) }
    single<AuthRepository> { AuthRepositorioRest(get(), get()) }
    // ProductoRepositorioEnMemoria sigue disponible como alternativa para pruebas.
    single<ProductoRepository> {
        val auth = get<AuthRepository>()
        ProductoRepositorioRest(get(), CATEGORIA_POR_DEFECTO) { mensaje -> auth.cerrarSesion(mensaje) }
    }
}

val domainModule = module {
    factoryOf(::ListarProductosUseCase)
    factoryOf(::ObtenerProductoUseCase)
    factoryOf(::RegistrarProductoUseCase)
    factoryOf(::ActualizarProductoUseCase)
    factoryOf(::EliminarProductoUseCase)
    factoryOf(::IniciarSesionUseCase)
    factoryOf(::CerrarSesionUseCase)
    factoryOf(::RestaurarSesionUseCase)
}

val presentationModule = module {
    viewModelOf(::ProductoViewModel)
    viewModelOf(::DetalleProductoViewModel)
    viewModel { SesionViewModel(get(), get(), get(), get(), get(named(ES_DEPURACION))) }
}

/** Categoría «Analgésicos» (id 1) creada en PharmaSoft; el dominio aún no maneja categorías. */
const val CATEGORIA_POR_DEFECTO = 1L

/** Nombre con el que cada plataforma registra la URL base de PharmaSoft. */
const val URL_BASE = "urlBase"

/** true en compilaciones de depuración: activa el log de cabeceras. */
const val ES_DEPURACION = "esDepuracion"

expect val platformModule: org.koin.core.module.Module

fun initKoin(config: KoinAppDeclaration? = null) = startKoin {
    config?.invoke(this)
    modules(dataModule, domainModule, presentationModule, platformModule)
}
