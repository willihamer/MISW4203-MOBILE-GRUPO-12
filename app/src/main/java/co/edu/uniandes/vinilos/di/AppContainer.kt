package co.edu.uniandes.vinilos.di

import android.content.Context
import co.edu.uniandes.vinilos.BuildConfig
import co.edu.uniandes.vinilos.data.network.NetworkServiceAdapter
import co.edu.uniandes.vinilos.data.network.VinilosApi
import co.edu.uniandes.vinilos.data.repository.AlbumRepository
import co.edu.uniandes.vinilos.data.repository.AlbumRepositoryImpl
import co.edu.uniandes.vinilos.data.repository.ArtistRepository
import co.edu.uniandes.vinilos.data.repository.ArtistRepositoryImpl
import co.edu.uniandes.vinilos.data.repository.CollectorRepository
import co.edu.uniandes.vinilos.data.repository.CollectorRepositoryImpl
import co.edu.uniandes.vinilos.data.repository.ProfileRepository
import co.edu.uniandes.vinilos.data.repository.ProfileRepositoryImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Inyección de dependencias manual: crea una sola instancia de cada servicio. */
class AppContainer(context: Context, baseUrl: String = BuildConfig.BASE_URL) {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            }
        }
        .build()

    private val api: VinilosApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(VinilosApi::class.java)

    private val serviceAdapter = NetworkServiceAdapter(api)

    val albumRepository: AlbumRepository = AlbumRepositoryImpl(serviceAdapter)
    val artistRepository: ArtistRepository = ArtistRepositoryImpl(serviceAdapter)
    val collectorRepository: CollectorRepository = CollectorRepositoryImpl(serviceAdapter)
    val profileRepository: ProfileRepository = ProfileRepositoryImpl(
        context.getSharedPreferences(ProfileRepository.PREFERENCES_NAME, Context.MODE_PRIVATE),
    )
}
