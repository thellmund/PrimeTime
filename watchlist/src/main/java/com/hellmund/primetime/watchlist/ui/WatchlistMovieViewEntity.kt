package com.hellmund.primetime.watchlist.ui

import android.os.Parcelable
import com.hellmund.primetime.data.model.HistoryMovie
import com.hellmund.primetime.data.model.Rating
import com.hellmund.primetime.data.model.WatchlistMovie
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDateTime

@Parcelize
data class WatchlistMovieViewEntity(
    val id: Int,
    val title: String,
    val posterUrl: String,
    val description: String,
    val hasRuntime: Boolean,
    val formattedRuntime: String,
    val formattedReleaseDate: String,
    val savedAt: LocalDateTime = LocalDateTime.now(),
    val notificationsActivated: Boolean = true,
    val isUnreleased: Boolean,
    val raw: WatchlistMovie
) : Parcelable {

    fun apply(rating: Rating) = RatedWatchlistMovie(this, rating)

}

data class RatedWatchlistMovie(val movie: WatchlistMovieViewEntity, val rating: Rating) {
    fun toHistoryMovie() = HistoryMovie(movie.id, movie.title, rating, LocalDateTime.now())
}
