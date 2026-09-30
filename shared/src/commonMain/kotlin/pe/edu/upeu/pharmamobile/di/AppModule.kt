package pe.edu.upeu.pharmamobile.di

import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobile.data.repository.ProductoRepositorioRest
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoViewModel

val dataModule = module {
    single { crearHttpClient(get(), get(named(URL_BASE))) }
    single { ProductoApi(get()) }
    single<ProductoRepository> { ProductoRepositorioRest(get()) }
}

val domainModule = module {
    factoryOf(::RegistrarProductoUseCase)
}

val presentationModule = module {
    viewModelOf(::ProductoViewModel)
}

/** Nombre con el que cada plataforma registra la URL base de PharmaSoft. */
const val URL_BASE = "urlBase"

expect val platformModule: org.koin.core.module.Module

fun initKoin(config: KoinAppDeclaration? = null) = startKoin {
    config?.invoke(this)
    modules(dataModule, domainModule, presentationModule, platformModule)
}
