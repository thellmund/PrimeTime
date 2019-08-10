package com.hellmund.primetime.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hellmund.api.DateSerializer
import com.hellmund.api.RetryInterceptor
import com.hellmund.api.TmdbApiService
import com.hellmund.api.TmdbInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.threeten.bp.LocalDate
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import javax.inject.Singleton

@Module
object NetworkModule {

    private const val BASE_URL = "https://api.themoviedb.org/3/"

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
    fun provideOkHttpClient(
        tmdbInterceptor: TmdbInterceptor,
        retryInterceptor: RetryInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(tmdbInterceptor)
            .addInterceptor(retryInterceptor)
            .addNetworkInterceptor(loggingInterceptor)
            .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, DateSerializer { Timber.i(it) })
            .create()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideTmdbApiService(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): TmdbApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(TmdbApiService::class.java)

}
