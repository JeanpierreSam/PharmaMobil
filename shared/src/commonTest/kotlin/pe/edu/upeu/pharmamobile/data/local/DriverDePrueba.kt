package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.db.SqlDriver
import pe.edu.upeu.pharmamobile.db.PharmaDb

/**
 * commonTest se ejecuta en la JVM (androidHostTest) y en iOS, y cada una abre
 * SQLite con un driver distinto: JdbcSqliteDriver solo existe en la JVM. Por eso
 * el driver de pruebas también es expect/actual, como el de la app.
 */
expect fun crearDriverDePrueba(): SqlDriver

/**
 * Una base NUEVA en memoria por prueba: si se compartiera, la primera prueba
 * pasaría y las siguientes fallarían por datos heredados.
 */
fun baseEnMemoria(): PharmaDb = PharmaDb(crearDriverDePrueba())
