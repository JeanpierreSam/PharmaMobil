package pe.edu.upeu.pharmamobile.di

import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobile.data.local.PerfilLocal
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.remote.AuthApi
import pe.edu.upeu.pharmamobile.data.remote.DiagnosticoApi
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobile.data.repository.AuthRepositorioRest
import pe.edu.upeu.pharmamobile.data.repository.ProductoRepositorioOffline
import pe.edu.upeu.pharmamobile.data.repository.ProductoRepositorioRest
import pe.edu.upeu.pharmamobile.db.PharmaDb
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ObservarProductosUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ObservarUltimaSincronizacionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RestaurarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.SincronizarProductosUseCase
import pe.edu.upeu.pharmamobile.presentation.detalle.DetalleProductoViewModel
import pe.edu.upeu.pharmamobile.presentation.diagnostico.DiagnosticoSesionViewModel
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobile.presentation.sesion.SesionViewModel

val dataModule = module {
    // Sesión 11: la base local. El driver lo aporta cada plataforma (platformModule).
    // Se crea al arrancar Koin: si el esquema está mal, la app falla aquí y no más adelante.
    single(createdAtStart = true) { PharmaDb(get<CreadorDeDriver>().crear()) }
    single { crearHttpClient(get(), get(named(URL_BASE)), get<AlmacenSeguro>(), get(named(ES_DEPURACION))) }
    single { ProductoApi(get()) }
    single { AuthApi(get()) }
    // Cliente sin plugin Auth, solo para el panel de diagnóstico (depuración).
    single(named(CLIENTE_DIAGNOSTICO)) {
        crearHttpClient(get(), get(named(URL_BASE)), almacen = null, esDepuracion = get(named(ES_DEPURACION)))
    }
    single { DiagnosticoApi(get(), get(named(CLIENTE_DIAGNOSTICO)), get(), get()) }
    single { ProductoLocal(get()) }
    single { PerfilLocal(get()) }
    single<AuthRepository> { AuthRepositorioRest(get(), get(), get<PerfilLocal>()) }
    // El acceso REST queda como «remoto» del repositorio offline.
    single {
        val auth = get<AuthRepository>()
        ProductoRepositorioRest(get(), CATEGORIA_POR_DEFECTO) { mensaje -> auth.cerrarSesion(mensaje) }
    }
    single<ProductoRepository> { ProductoRepositorioOffline(get(), get()) }
}

val domainModule = module {
    factoryOf(::ObservarProductosUseCase)
    factoryOf(::SincronizarProductosUseCase)
    factoryOf(::ObservarUltimaSincronizacionUseCase)
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
    viewModelOf(::DiagnosticoSesionViewModel)
    viewModel { SesionViewModel(get(), get(), get(), get(), get(named(ES_DEPURACION))) }
}

/** Categoría «Analgésicos» (id 1) creada en PharmaSoft; el dominio aún no maneja categorías. */
const val CATEGORIA_POR_DEFECTO = 1L

/** Nombre con el que cada plataforma registra la URL base de PharmaSoft. */
const val URL_BASE = "urlBase"

/** true en compilaciones de depuración: activa el log de cabeceras. */
const val ES_DEPURACION = "esDepuracion"

/** HttpClient sin Auth del panel de diagnóstico. */
const val CLIENTE_DIAGNOSTICO = "clienteDiagnostico"

expect val platformModule: org.koin.core.module.Module

fun initKoin(config: KoinAppDeclaration? = null) = startKoin {
    config?.invoke(this)
    modules(dataModule, domainModule, presentationModule, platformModule)
}
