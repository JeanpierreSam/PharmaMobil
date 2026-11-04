package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import pe.edu.upeu.pharmamobile.db.PharmaDb

/** JVM: base en memoria con el driver JDBC; el esquema se crea a mano. */
actual fun crearDriverDePrueba(): SqlDriver {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    PharmaDb.Schema.create(driver)
    return driver
}
