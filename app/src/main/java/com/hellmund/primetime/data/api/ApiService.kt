package com.hellmund.primetime.data.api

import com.hellmund.primetime.data.model.GenresResponse
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.model.SamplesResponse
import com.hellmund.primetime.ui.main.data.MoviesResponse
import com.hellmund.primetime.ui.main.VideosResponse
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
    fun upcoming(): Observable<MoviesResponse>

    @GET("movie/now_playing")
    fun nowPlaying(): Observable<MoviesResponse>

    @GET("movie/top_rated")
    fun topRatedMovies(): Observable<MoviesResponse>

    @GET("movie/{movieId}/recommendations")
    fun recommendations(
            @Path("movieId") movieId: Int,
            @Query("sort_by") sortBy: String = "popularity.desc"
    ): Observable<MoviesResponse>

    @GET("genre/{genreId}/movies")
    fun genreRecommendations(
            @Path("genreId") genreId: Int
    ): Observable<MoviesResponse>

    @GET("movie/{movieId}/videos")
    fun videos(@Path("movieId") movieId: Int): Observable<VideosResponse>

    @GET("movie/{movieId}")
    fun movie(@Path("movieId") movieId: Int): Observable<Movie>

    @GET("search/movie")
    fun search(@Query("query") query: String): Observable<MoviesResponse>

    @GET("movie/popular")
    fun popular(): Observable<MoviesResponse>

}
