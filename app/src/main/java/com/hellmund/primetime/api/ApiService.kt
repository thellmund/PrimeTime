package com.hellmund.primetime.api

import com.google.gson.GsonBuilder
import com.hellmund.primetime.main.RecommendationsResponse
import com.hellmund.primetime.main.VideosResponse
import com.hellmund.primetime.model2.ApiMovie
import com.hellmund.primetime.model2.GenresResponse
import com.hellmund.primetime.model2.SamplesResponse
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*

interface ApiService {

    @GET("genre/movie/list")
    fun genres(): Observable<GenresResponse>

    @GET("discover/movie")
    fun discoverMovies(
            @Query("with_genres") withGenres: Int? = null,
            @Query("sort_by") sortBy: String = "popularity.desc",
            @Query("primary_release_year") releaseYear: Int? = null
    ): Observable<SamplesResponse>

    @GET("movie/upcoming")
    fun upcoming(): Observable<RecommendationsResponse>

    @GET("movie/now_playing")
    fun nowPlaying(): Observable<RecommendationsResponse>

    @GET("movie/top_rated")
    fun topRatedMovies(): Observable<RecommendationsResponse>

    @GET("movie/{movieId}/recommendations")
    fun recommendations(
            @Path("movieId") movieId: Int,
            @Query("sort_by") sortBy: String = "popularity.desc"
    ): Observable<RecommendationsResponse>

    @GET("genre/{genreId}/movies")
    fun genreRecommendations(
            @Path("genreId") genreId: Int
    ): Observable<RecommendationsResponse>

    @GET("movie/{movieId}/videos")
    fun videos(@Path("movieId") movieId: Int): Observable<VideosResponse>

    @GET("movie/{movieId}")
    fun movie(@Path("movieId") movieId: Int): Observable<ApiMovie>

    @GET("search/movie")
    fun search(@Query("query") query: String): Observable<RecommendationsResponse>

}

object ApiClient {

    private const val BASE_URL = "http://api.themoviedb.org/3/"

    @JvmStatic
    val instance: ApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(TmdbInterceptor())
                .addNetworkInterceptor(loggingInterceptor)
                .build()

        val gson = GsonBuilder()
                .registerTypeAdapter(Date::class.java, DateSerializer())
                .create()

        Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService::class.java)
    }

}
