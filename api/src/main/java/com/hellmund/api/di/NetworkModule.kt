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
        tmdbApiKey: String,
        retryInterceptor: RetryInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        dateSerializer: DateSerializer
    ): HttpClient = HttpClient(OkHttp) {
        install(DefaultRequest) {
            parameter("api_key", tmdbApiKey)
            parameter("language", Locale.getDefault().language)
        }

        install(JsonFeature) {
            serializer = GsonSerializer {
                registerTypeAdapter(LocalDate::class.java, dateSerializer)
            }
        }

        engine {
            addInterceptor(retryInterceptor)
            addNetworkInterceptor(loggingInterceptor)
        }
    }
}
