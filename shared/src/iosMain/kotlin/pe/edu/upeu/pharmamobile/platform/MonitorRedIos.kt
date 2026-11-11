package pe.edu.upeu.pharmamobile.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pe.edu.upeu.pharmamobile.domain.platform.MonitorRed

/**
 * Sin Mac no se puede probar NWPathMonitor (H13): en iOS se asume red y el
 * usuario sincroniza con ⟳. Queda declarado como limitación.
 */
class MonitorRedIos : MonitorRed {
    override val hayRed: Flow<Boolean> = flowOf(true)
}
