package com.hellmund.api.di

import com.hellmund.api.DateSerializer
import dagger.Module
import dagger.Provides
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.DefaultRequest
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.parameter
import org.threeten.bp.LocalDate
import java.util.Locale
import javax.inject.Singleton

@Module
object NetworkModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideHttpClient() = HttpClient(Android) {
        install(DefaultRequest) {
            parameter("api_key", "7564dba629324e3048f362a03c8a76bc") // TODO
            parameter("language", Locale.getDefault().language)
        }

        install(JsonFeature) {
            serializer = GsonSerializer {
                registerTypeAdapter(LocalDate::class.java, DateSerializer())
            }
        }

        // TODO Add retry interceptor
    }

}
