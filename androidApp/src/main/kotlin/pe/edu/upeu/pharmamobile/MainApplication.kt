package pe.edu.upeu.pharmamobile

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.edu.upeu.pharmamobile.di.URL_BASE
import pe.edu.upeu.pharmamobile.di.initKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MainApplication)
            androidLogger(if (BuildConfig.DEBUG) Level.INFO else Level.ERROR)
            // Sesión 14: URL_BASE sale de local.properties → BuildConfig (paso 1.4).
            modules(module { single(named(URL_BASE)) { BuildConfig.URL_PHARMASOFT } })
        }
    }
}
