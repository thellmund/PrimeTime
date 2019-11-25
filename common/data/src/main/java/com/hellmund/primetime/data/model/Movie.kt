package com.hellmund.primetime.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.hellmund.api.model.ApiMovie
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.threeten.bp.LocalDate

@Parcelize
data class Movie(
    val id: Long,
    @SerializedName("poster_path") val posterPath: String,
    @SerializedName("backdrop_path") val backdropPath: String,
    val title: String,
    @SerializedName("genre_ids") val genreIds: List<Long>? = emptyList(),
    @SerializedName("genres") val genres: @RawValue List<Genre>? = emptyList(), // TODO RawValue?
    @SerializedName("overview") val description: String,
    @SerializedName("release_date") val releaseDate: LocalDate?,
    val popularity: Float,
    @SerializedName("vote_average") val voteAverage: Float,
    @SerializedName("vote_count") val voteCount: Int,
    val runtime: Int? = null,
    @SerializedName("imdb_id") val imdbId: String? = null
) : Parcelable {

    val fullPosterUrl: String
        get() = "https://image.tmdb.org/t/p/w500$posterPath"

    enum class WatchStatus {
        NOT_WATCHED, ON_WATCHLIST, WATCHED
    }

    companion object {

        fun from(apiMovie: ApiMovie) = Movie(
            apiMovie.id,
            checkNotNull(apiMovie.posterPath),
            checkNotNull(apiMovie.backdropPath),
            apiMovie.title,
            apiMovie.genreIds,
            apiMovie.genres.orEmpty().map {
                Genre.Impl(id = it.id, name = it.name, isPreferred = false, isExcluded = true)
            },
            apiMovie.description,
            apiMovie.releaseDate,
            apiMovie.popularity,
            apiMovie.voteAverage,
            apiMovie.voteCount,
            apiMovie.runtime,
            apiMovie.imdbId
        )

    }

}