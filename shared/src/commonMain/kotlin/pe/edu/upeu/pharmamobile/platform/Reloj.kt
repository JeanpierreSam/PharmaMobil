package pe.edu.upeu.pharmamobile.platform

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Instante actual en milisegundos (epoch). Igual en Android e iOS. */
@OptIn(ExperimentalTime::class)
fun ahoraEnMilis(): Long = Clock.System.now().toEpochMilliseconds()
