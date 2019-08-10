package com.hellmund.api

import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDate

data class MoviesResponse(val results: List<ApiMovie>)

data class ApiMovie(
    val id: Int,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    val title: String,
    @SerializedName("genre_ids") val genreIds: List<Int>? = emptyList(),
    @SerializedName("genres") val genres: List<ApiGenre>? = emptyList(),
    @SerializedName("overview") val description: String,
    @SerializedName("release_date") val releaseDate: LocalDate?,
    val popularity: Float,
    @SerializedName("vote_average") val voteAverage: Float,
    @SerializedName("vote_count") val voteCount: Int,
    val runtime: Int? = null,
    @SerializedName("imdb_id") val imdbId: String? = null
) {

    val isValid: Boolean
        get() = posterPath != null && backdropPath != null

}
