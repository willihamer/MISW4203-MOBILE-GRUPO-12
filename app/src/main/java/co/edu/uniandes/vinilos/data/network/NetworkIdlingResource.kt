package co.edu.uniandes.vinilos.data.network

import androidx.test.espresso.idling.CountingIdlingResource

/**
 * Cuenta las peticiones en curso para que las pruebas de Espresso esperen a la red
 * sin usar sleep. Fuera de las pruebas nadie lo registra y no tiene efecto.
 */
object NetworkIdlingResource {
    val resource = CountingIdlingResource("network")

    fun increment() = resource.increment()

    fun decrement() {
        if (!resource.isIdleNow) resource.decrement()
    }
}
