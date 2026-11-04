package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.inMemoryDriver
import pe.edu.upeu.pharmamobile.db.PharmaDb

/** iOS: el driver nativo en memoria ya crea el esquema. */
actual fun crearDriverDePrueba(): SqlDriver = inMemoryDriver(PharmaDb.Schema)
