package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.db.SqlDriver

/**
 * El esquema y las consultas viven en commonMain; lo único que cambia por
 * plataforma es quién abre el archivo pharma.db. Mismo patrón que AlmacenSeguro.
 */
interface CreadorDeDriver {
    fun crear(): SqlDriver
}
