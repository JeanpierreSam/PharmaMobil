package pe.edu.upeu.pharmamobile.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pe.edu.upeu.pharmamobile.domain.platform.MonitorRed

/** Modo avión a voluntad: estado.value = false / true. */
class FakeMonitorRed(inicial: Boolean = true) : MonitorRed {
    val estado = MutableStateFlow(inicial)
    override val hayRed: Flow<Boolean> = estado
}
