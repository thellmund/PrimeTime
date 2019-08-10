package com.hellmund.api

import com.hellmund.api.model.ApiMovie
import com.hellmund.api.model.GenresResponse
import com.hellmund.api.model.MoviesResponse
import com.hellmund.api.model.ReviewsResponse
import com.hellmund.api.model.SamplesResponse
import com.hellmund.api.model.VideosResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

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

    @GET("movie/now_playing")
    suspend fun nowPlaying(
        @Query("page") page: Int
    ): MoviesResponse

    @GET("movie/top_rated")
    suspend fun topRatedMovies(
        @Query("page") page: Int
    ): MoviesResponse

    @GET("movie/{movieId}/recommendations")
    suspend fun recommendations(
        @Path("movieId") movieId: Int,
        @Query("page") page: Int,
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): MoviesResponse

    @GET("genre/{genreId}/movies")
    suspend fun genreRecommendations(
        @Path("genreId") genreId: Int,
        @Query("page") page: Int
    ): MoviesResponse

    @GET("movie/{movieId}/videos")
    suspend fun videos(@Path("movieId") movieId: Int): VideosResponse

    @GET("movie/{movieId}")
    suspend fun movie(@Path("movieId") movieId: Int): ApiMovie

    @GET("search/movie")
    suspend fun search(@Query("query") query: String): MoviesResponse

    @GET("movie/popular")
    suspend fun popular(): MoviesResponse

    @GET("movie/{movieId}/reviews")
    suspend fun reviews(@Path("movieId") movieId: Int): ReviewsResponse

}
