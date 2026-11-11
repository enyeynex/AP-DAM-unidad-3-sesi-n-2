package pe.edu.upeu.pharmamobil

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import pe.edu.upeu.pharmamobil.di.initKoin
import pe.edu.upeu.pharmamobil.permiso.moduloPermisosAndroid
import pe.edu.upeu.pharmamobil.platform.ConfiguracionRed

/**
 * Arranca Koin una sola vez, antes que cualquier Activity. Se declara en el
 * manifiesto con android:name=".MainApplication".
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Guía 14: URL de PharmaSoft elegida al compilar (-PurlServicio=...).
        // Va antes de Koin para que el cliente HTTP nazca con ella.
        ConfiguracionRed.urlServicio = BuildConfig.URL_SERVICIO

        initKoin {
            androidLogger()
            androidContext(this@MainApplication)
            // Permisos y notificaciones: solo existen en la aplicación Android.
            modules(moduloPermisosAndroid)
        }
    }
}
