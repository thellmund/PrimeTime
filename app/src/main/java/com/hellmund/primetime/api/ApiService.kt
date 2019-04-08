package com.hellmund.primetime.api

import com.google.gson.GsonBuilder
import com.hellmund.primetime.model2.GenresResponse
import com.hellmund.primetime.model2.SamplesResponse
import io.reactivex.Observable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("genre/movie/list")
    fun genres(): Observable<GenresResponse>

    @GET("discover/movie")
    fun discoverMovies(
            @Query("with_genres") withGenres: String? = null,
            @Query("sort_by") sortBy: String = "popularity.desc",
            @Query("primary_release_year") releaseYear: Int? = null
    ): Observable<SamplesResponse>

}

object ApiClient {

    private const val BASE_URL = "http://api.themoviedb.org/3/"

    @JvmStatic
    val instance: ApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(TmdbInterceptor())
                .build()

        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd").create()

        Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService::class.java)
    }

}
