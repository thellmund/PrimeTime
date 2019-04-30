package com.hellmund.primetime.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate

@Parcelize
data class ApiMovie(
        val id: Int,
        @SerializedName("poster_path") val posterPath: String,
        val title: String,
        @SerializedName("genre_ids") val genreIds: List<Int>,
        @SerializedName("genres") val genres: List<Genre>,
        @SerializedName("overview") val description: String,
        @SerializedName("release_date") val releaseDate: LocalDate?,
        val popularity: Float,
        @SerializedName("vote_average") val voteAverage: Float,
        val runtime: Int? = null,
        @SerializedName("imdb_id") val imdbId: String? = null
) : Parcelable {

    val fullPosterUrl: String
        get() = "http://image.tmdb.org/t/p/w500$posterPath"

    enum class WatchStatus {
        NOT_WATCHED, ON_WATCHLIST, WATCHED
    }

}
