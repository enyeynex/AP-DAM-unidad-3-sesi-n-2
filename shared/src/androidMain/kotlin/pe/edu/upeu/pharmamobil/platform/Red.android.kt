package pe.edu.upeu.pharmamobil.platform

/**
 * Guía 14: la URL del servicio la decide el build de la app Android
 * (BuildConfig.URL_SERVICIO, propiedad Gradle urlServicio) y se fija aquí en
 * MainApplication.onCreate, antes de crear el cliente HTTP. Por defecto,
 * 10.0.2.2 es el equipo anfitrión visto desde el emulador.
 */
object ConfiguracionRed {
    @Volatile
    var urlServicio: String = "http://10.0.2.2:8080/api/v1/"
}

actual val URL_BASE: String
    get() = ConfiguracionRed.urlServicio
