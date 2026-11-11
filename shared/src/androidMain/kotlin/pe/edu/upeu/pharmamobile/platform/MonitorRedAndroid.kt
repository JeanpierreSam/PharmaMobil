package pe.edu.upeu.pharmamobile.platform

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import pe.edu.upeu.pharmamobile.domain.platform.MonitorRed

/**
 * Escucha la red por defecto. Se usa NET_CAPABILITY_INTERNET y no VALIDATED:
 * con la zona con cobertura del PC sin internet, la red no queda «validada»,
 * pero PharmaSoft (en la red local) sí es alcanzable.
 */
class MonitorRedAndroid(contexto: Context) : MonitorRed {

    private val gestor = contexto.getSystemService(ConnectivityManager::class.java)

    override val hayRed: Flow<Boolean> = callbackFlow {
        fun conectado(): Boolean {
            val capacidades = gestor.getNetworkCapabilities(gestor.activeNetwork) ?: return false
            return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(conectado())   // puede haber pasado de Wi-Fi a datos
            }
        }
        trySend(conectado())
        gestor.registerDefaultNetworkCallback(callback)
        awaitClose { gestor.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
