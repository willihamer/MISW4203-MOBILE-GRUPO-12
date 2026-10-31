package co.edu.uniandes.vinilos

import android.app.Application
import co.edu.uniandes.vinilos.di.AppContainer

open class VinilosApplication : Application() {
    lateinit var container: AppContainer
        protected set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
