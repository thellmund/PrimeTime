package com.hellmund.primetime.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class ApiMovie(
        val id: Int,
        @SerializedName("poster_path") val posterPath: String,
        val title: String,
        @SerializedName("genre_ids") val genreIds: List<Int>,
        @SerializedName("overview") val description: String,
        @SerializedName("release_date") val releaseDate: Date?,
        val popularity: Float,
        @SerializedName("vote_average") val voteAverage: Float,
        val runtime: Int? = null,
        @SerializedName("imdb_id") val imdbId: String? = null
) : Parcelable {

    val fullPosterUrl: String
        get() = "http://image.tmdb.org/t/p/w500$posterPath"

    fun getPrettyRuntime(): String {
        val runtime = runtime ?: throw IllegalStateException()
        val hours = String.format(Locale.getDefault(), "%01d", runtime / 60)
        val minutes = String.format(Locale.getDefault(), "%02d", runtime % 60)
        return String.format("%s:%s", hours, minutes)
    }

    enum class WatchStatus {
        NOT_WATCHED, ON_WATCHLIST, WATCHED
    }

}
