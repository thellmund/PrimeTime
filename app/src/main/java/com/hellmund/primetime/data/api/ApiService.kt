package com.hellmund.primetime.data.api

import com.hellmund.primetime.data.model.GenresResponse
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.ui.selectmovies.SamplesResponse
import com.hellmund.primetime.ui.suggestions.VideosResponse
import com.hellmund.primetime.ui.suggestions.data.MoviesResponse
import com.hellmund.primetime.ui.suggestions.details.ReviewsResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("genre/movie/list")
    suspend fun genres(): GenresResponse

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("with_genres") genre: Int? = null,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("primary_release_year") releaseYear: Int? = null,
        @Query("page") page: Int = 1
    ): SamplesResponse

    @GET("movie/upcoming")
    suspend fun upcoming(
        @Query("page") page: Int
    ): MoviesResponse

    @GET("movie/upcoming")
    fun upcomingRx(
        @Query("page") page: Int
    ): Observable<MoviesResponse>

    @GET("movie/now_playing")
    suspend fun nowPlaying(
        @Query("page") page: Int
    ): MoviesResponse

    @GET("movie/now_playing")
    fun nowPlayingRx(
        @Query("page") page: Int
    ): Observable<MoviesResponse>

    @GET("movie/top_rated")
    suspend fun topRatedMovies(
        @Query("page") page: Int
    ): MoviesResponse

    @GET("movie/top_rated")
    fun topRatedMoviesRx(
        @Query("page") page: Int
    ): Observable<MoviesResponse>

    @GET("movie/{movieId}/recommendations")
    suspend fun recommendations(
        @Path("movieId") movieId: Int,
        @Query("page") page: Int,
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): MoviesResponse

    @GET("movie/{movieId}/recommendations")
    fun recommendationsRx(
        @Path("movieId") movieId: Int,
        @Query("page") page: Int,
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): Observable<MoviesResponse>

    @GET("genre/{genreId}/movies")
    suspend fun genreRecommendations(
        @Path("genreId") genreId: Int,
        @Query("page") page: Int
    ): MoviesResponse

    @GET("genre/{genreId}/movies")
    fun genreRecommendationsRx(
        @Path("genreId") genreId: Int,
        @Query("page") page: Int
    ): Observable<MoviesResponse>

    @GET("movie/{movieId}/videos")
    suspend fun videos(@Path("movieId") movieId: Int): VideosResponse

    @GET("movie/{movieId}")
    suspend fun movie(@Path("movieId") movieId: Int): Movie

    @GET("search/movie")
    suspend fun search(@Query("query") query: String): MoviesResponse

    @GET("movie/popular")
    suspend fun popular(): MoviesResponse

    @GET("movie/{movieId}/reviews")
    suspend fun reviews(@Path("movieId") movieId: Int): ReviewsResponse

}
