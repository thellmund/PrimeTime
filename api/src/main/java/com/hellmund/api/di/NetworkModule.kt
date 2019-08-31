package com.hellmund.api.di

import com.hellmund.api.DateSerializer
import com.hellmund.api.RetryInterceptor
import dagger.Module
import dagger.Provides
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.DefaultRequest
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.parameter
import okhttp3.logging.HttpLoggingInterceptor
import org.threeten.bp.LocalDate
import java.util.Locale
import javax.inject.Singleton

@Module
object NetworkModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideHttpClient(
        retryInterceptor: RetryInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ) = HttpClient(OkHttp) {
        install(DefaultRequest) {
            parameter("api_key", "7564dba629324e3048f362a03c8a76bc") // TODO
            parameter("language", Locale.getDefault().language)
        }

        install(JsonFeature) {
            serializer = GsonSerializer {
                registerTypeAdapter(LocalDate::class.java, DateSerializer())
            }
        }

        engine {
            addInterceptor(retryInterceptor)
            addNetworkInterceptor(loggingInterceptor)
        }
    }

}
