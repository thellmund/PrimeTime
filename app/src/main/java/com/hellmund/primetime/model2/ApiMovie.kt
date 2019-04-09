package com.hellmund.primetime.model2

import android.content.Context
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.hellmund.primetime.R
import com.hellmund.primetime.utils.DateUtils
import com.hellmund.primetime.utils.GenreUtils
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class ApiMovie(
        val id: Int,
        @SerializedName("poster_path") val posterUrl: String,
        val title: String,
        @SerializedName("genre_ids") val genreIds: List<Int>,
        @SerializedName("overview") val description: String,
        @SerializedName("release_date") val releaseDate: Date?,
        val popularity: Float,
        @SerializedName("vote_average") val voteAverage: Float,
        val runtime: Int? = null,
        @SerializedName("imdb_id") val imdbId: String? = null
) : Parcelable {

    val hasAdditionalInformation: Boolean
        get() {
            return runtime != null && imdbId != null
        }

    fun getPrettyGenres(context: Context): String {
        return genreIds
                .map { GenreUtils.getGenreName(context, it) }
                .sorted()
                .joinToString(", ")
    }

    fun getPrettyRuntime(): String {
        val runtime = runtime ?: throw IllegalStateException()
        val hours = String.format(Locale.getDefault(), "%01d", runtime / 60)
        val minutes = String.format(Locale.getDefault(), "%02d", runtime % 60)
        return String.format("%s:%s", hours, minutes)
    }

    fun getPrettyVoteAverage(): String = "$voteAverage / 10"

    fun getReleaseYear(context: Context): String {
        if (releaseDate == null) {
            return context.getString(R.string.no_information)
        }

        val release = Calendar.getInstance()
        release.time = releaseDate

        val now = Calendar.getInstance()

        return if (release.after(now)) {
            DateUtils.getDateInLocalFormat(release)
        } else {
            Integer.toString(release.get(Calendar.YEAR))
        }
    }

    enum class WatchStatus {
        NOT_WATCHED, ON_WATCHLIST, WATCHED
    }

}
