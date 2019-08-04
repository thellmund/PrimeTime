package com.hellmund.primetime.ui.watchlist

import android.os.Parcelable
import com.hellmund.primetime.data.database.WatchlistMovie
import com.hellmund.primetime.data.model.Rating
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

data class RatedWatchlistMovie(val movie: WatchlistMovieViewEntity, val rating: Rating)
