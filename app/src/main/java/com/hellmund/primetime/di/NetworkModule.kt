package com.hellmund.primetime.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hellmund.primetime.data.api.ApiService
import com.hellmund.primetime.data.api.DateSerializer
import com.hellmund.primetime.data.api.RetryInterceptor
import com.hellmund.primetime.data.api.TmdbInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.threeten.bp.LocalDate
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class NetworkModule {

    @Singleton
    @Provides
    fun provideTmdbInterceptor(): TmdbInterceptor = TmdbInterceptor()

    @Singleton
    @Provides
    fun provideRetryInterceptor(): RetryInterceptor = RetryInterceptor()

    @Singleton
    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }

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

    @Singleton
    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
                .registerTypeAdapter(LocalDate::class.java, DateSerializer())
                .create()
    }

    @Singleton
    @Provides
    fun provideApiService(
            okHttpClient: OkHttpClient,
            gson: Gson
    ): ApiService {
        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService::class.java)
    }

    companion object {
        private const val BASE_URL = "https://api.themoviedb.org/3/"
    }

}
