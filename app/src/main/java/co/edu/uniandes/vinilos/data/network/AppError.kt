package co.edu.uniandes.vinilos.data.network

/** Errores propios de la app: las capas superiores no dependen de OkHttp ni de Retrofit. */
sealed class AppError : Exception() {
    /** Sin red, servidor apagado o tiempo de espera agotado. */
    class NoConnection : AppError()

    /** El servidor respondió 4xx o 5xx. */
    class Server(val code: Int) : AppError()

    /** Cualquier otra falla, p. ej. un JSON que no se pudo leer. */
    class Unknown : AppError()
}
