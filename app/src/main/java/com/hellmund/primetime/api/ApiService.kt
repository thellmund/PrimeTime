package com.hellmund.primetime.api

import com.hellmund.primetime.main.RecommendationsResponse
import com.hellmund.primetime.main.VideosResponse
import com.hellmund.primetime.model.ApiMovie
import com.hellmund.primetime.model.GenresResponse
import com.hellmund.primetime.model.SamplesResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("movie/popular")
    fun popular(): Observable<RecommendationsResponse>

}
