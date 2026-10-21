package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import pe.edu.upeu.pharmamobile.db.PharmaDb

/** No necesita contexto: usa el sistema de archivos de la app. */
class CreadorDeDriverIos : CreadorDeDriver {

    override fun crear(): SqlDriver = NativeSqliteDriver(
        schema = PharmaDb.Schema,
        name = "pharma.db"
    )
}
