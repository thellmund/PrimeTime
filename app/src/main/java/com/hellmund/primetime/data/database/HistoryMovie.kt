package com.hellmund.primetime.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.ui.suggestions.details.Rating
import com.hellmund.primetime.ui.watchlist.WatchlistMovieViewEntity
import org.threeten.bp.LocalDate

@Entity(tableName = "history_movies")
data class HistoryMovie(
        @PrimaryKey var id: Int,
        var title: String,
        var rating: Int,
        var timestamp: LocalDate,
        var isUpdating: Boolean
): Comparable<HistoryMovie> {

    override fun compareTo(other: HistoryMovie): Int = other.timestamp.compareTo(timestamp)

    companion object {

        fun fromMovie(movie: Movie, rating: Int): HistoryMovie {
            return HistoryMovie(movie.id, movie.title, rating, LocalDate.now(), false)
        }

        fun fromRating(rating: Rating): HistoryMovie {
            val movie = rating.movie
            val ratingValue = if (rating is Rating.Like) 1 else 0
            return HistoryMovie(movie.id, movie.title, ratingValue, LocalDate.now(), false)
        }

        fun fromWatchlistMovie(movie: WatchlistMovieViewEntity, rating: Int): HistoryMovie {
            return HistoryMovie(movie.id, movie.title, rating, LocalDate.now(), false)
        }

    }

}
