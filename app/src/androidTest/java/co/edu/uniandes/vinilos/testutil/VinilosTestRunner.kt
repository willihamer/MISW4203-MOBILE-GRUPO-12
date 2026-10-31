package co.edu.uniandes.vinilos.testutil

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import co.edu.uniandes.vinilos.BuildConfig
import co.edu.uniandes.vinilos.VinilosApplication
import co.edu.uniandes.vinilos.di.AppContainer

class VinilosTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application =
        super.newApplication(cl, TestVinilosApplication::class.java.name, context)
}

/** Solo se empaqueta en el APK de pruebas; cada caso recibe cachés independientes. */
class TestVinilosApplication : VinilosApplication() {
    fun configure(baseUrl: String = BuildConfig.BASE_URL) { container = AppContainer(this, baseUrl) }
}
