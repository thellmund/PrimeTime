package com.hellmund.api

import com.hellmund.api.model.ApiMovie
import com.hellmund.api.model.GenresResponse
import com.hellmund.api.model.MoviesResponse
import com.hellmund.api.model.ReviewsResponse
import com.hellmund.api.model.SamplesResponse
import com.hellmund.api.model.VideosResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import javax.inject.Inject

class TmdbApiService @Inject constructor(
    private val client: HttpClient
) {

    suspend fun genres(): GenresResponse = client.get(path("genre/movie/list"))

    suspend fun discoverMovies(
        genre: Long? = null,
        sortBy: String = "popularity.desc",
        releaseYear: Int? = null,
        page: Int = 1
    ): SamplesResponse = client.get(path("discover/movie")) {
        parameter("with_genres", genre)
        parameter("sort_by", sortBy)
        parameter("primary_release_year", releaseYear)
        parameter("page", page)
    }

    suspend fun upcoming(
        page: Int
    ): MoviesResponse = client.get(path("movie/upcoming")) {
        parameter("page", page)
    }

    suspend fun nowPlaying(
        page: Int
    ): MoviesResponse = client.get(path("movie/now_playing")) {
        parameter("page", page)
    }

    suspend fun topRatedMovies(
        page: Int
    ): MoviesResponse = client.get(path("movie/top_rated")) {
        parameter("page", page)
    }

    suspend fun recommendations(
        movieId: Long,
        page: Int,
        sortBy: String = "popularity.desc"
    ): MoviesResponse = client.get() {
        url(path("movie/$movieId/recommendations"))
        parameter("page", page)
        parameter("sort_by", sortBy)
    }

    suspend fun genreRecommendations(
        genreId: Long,
        page: Int
    ): MoviesResponse = client.get() {
        url(path("genre/$genreId/movies"))
        parameter("page", page)
    }

    suspend fun videos(
        movieId: Long
    ): VideosResponse = client.get() {
        url(path("movie/$movieId/videos"))
    }

    suspend fun movie(
        movieId: Long
    ): ApiMovie = client.get() {
        url(path("movie/$movieId"))
    }

    suspend fun search(
        query: String
    ): MoviesResponse = client.get(path("search/movie")) {
        parameter("query", query)
    }

    suspend fun popular(): MoviesResponse = client.get(path("movie/popular"))

    suspend fun reviews(
        movieId: Long
    ): ReviewsResponse = client.get() {
        url(path("movie/$movieId/reviews"))
    }

    private fun path(path: String) = "https://api.themoviedb.org/3/$path"

}
