package com.hellmund.api.model

import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDate

data class MoviesResponse(val results: List<PartialApiMovie>)

data class PartialApiMovie(
    @SerializedName("id") val id: Long,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("title") val title: String,
    @SerializedName("genre_ids") val genreIds: List<Long>,
    @SerializedName("overview") val description: String,
    @SerializedName("release_date") val releaseDate: LocalDate,
    @SerializedName("popularity") val popularity: Float,
    @SerializedName("vote_average") val voteAverage: Float,
    @SerializedName("vote_count") val voteCount: Int
)

data class FullApiMovie(
    @SerializedName("id") val id: Long,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("title") val title: String,
    @SerializedName("genres") val genres: List<ApiGenre>,
    @SerializedName("overview") val description: String,
    @SerializedName("release_date") val releaseDate: LocalDate,
    @SerializedName("popularity") val popularity: Float,
    @SerializedName("vote_average") val voteAverage: Float,
    @SerializedName("vote_count") val voteCount: Int,
    @SerializedName("runtime") val runtime: Int,
    @SerializedName("imdb_id") val imdbId: String
)
