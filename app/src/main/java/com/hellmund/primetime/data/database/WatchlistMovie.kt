package com.hellmund.primetime.data.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcelable
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.utils.isAfterNow
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

@Parcelize
@Entity(tableName = "watchlist_movies")
data class WatchlistMovie(
        @PrimaryKey var id: Int,
        var title: String,
        var posterURL: String,
        var runtime: Int,
        var releaseDate: LocalDate,
        var timestamp: LocalDateTime = LocalDateTime.now(),
        var deleted: Boolean = false,
        var notificationsActivated: Boolean = true
): Parcelable {

    val fullPosterUrl: String
        get() = "http://image.tmdb.org/t/p/w500$posterURL"

    val isUnreleased: Boolean
        get() = releaseDate.isAfterNow

    val hasRuntime: Boolean
        get() = true // TODO

    val isNotificationActivited: Boolean
        get() = notificationsActivated

    companion object {

        fun from(movie: Movie): WatchlistMovie {
            return WatchlistMovie(
                    movie.id,
                    movie.title,
                    movie.posterPath,
                    movie.runtime ?: -1, // TODO
                    movie.releaseDate ?: LocalDate.now())
        }

    }

}
