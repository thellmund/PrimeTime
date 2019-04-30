package com.hellmund.primetime.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcelable
import com.hellmund.primetime.model.ApiMovie
import com.hellmund.primetime.utils.DateUtils
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "watchlist_movies")
data class WatchlistMovie(
        @PrimaryKey var id: Int,
        var title: String,
        var posterURL: String,
        var runtime: Int,
        var releaseDate: Date,
        var timestamp: Date = Date(),
        var deleted: Boolean = false,
        var notificationsActivated: Boolean = true
): Parcelable {

    val fullPosterUrl: String
        get() = "http://image.tmdb.org/t/p/w500$posterURL"

    val isUnreleased: Boolean
        get() {
            val today = DateUtils.startOfDay().time
            return releaseDate.after(today)
        }

    val hasRuntime: Boolean
        get() = true // TODO

    val isNotificationActivited: Boolean
        get() = notificationsActivated

    companion object {

        fun from(movie: ApiMovie): WatchlistMovie {
            return WatchlistMovie(
                    movie.id,
                    movie.title,
                    movie.posterPath,
                    movie.runtime ?: -1, // TODO
                    movie.releaseDate ?: Date())
        }

    }

}
