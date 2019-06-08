package com.hellmund.primetime.data.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hellmund.primetime.data.model.Movie
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

@Parcelize
@Entity(tableName = "watchlist_movies")
data class WatchlistMovie(
        @PrimaryKey var id: Int,
        var title: String,
        var posterURL: String,
        var description: String,
        var runtime: Int,
        var releaseDate: LocalDate,
        var timestamp: LocalDateTime = LocalDateTime.now(),
        var deleted: Boolean = false,
        var notificationsActivated: Boolean = true
): Parcelable {

    companion object {

        fun from(movie: Movie): WatchlistMovie {
            return WatchlistMovie(
                    movie.id,
                    movie.title,
                    movie.posterPath,
                    movie.description,
                    movie.runtime ?: -1, // TODO
                    movie.releaseDate ?: LocalDate.now())
        }

    }

}
