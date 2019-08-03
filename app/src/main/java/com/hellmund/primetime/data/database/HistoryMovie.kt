package com.hellmund.primetime.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hellmund.primetime.ui.suggestions.details.Rating
import com.hellmund.primetime.ui.watchlist.WatchlistMovieViewEntity
import org.threeten.bp.LocalDateTime

@Entity(tableName = "history_movies")
data class HistoryMovie(
    @PrimaryKey var id: Int,
    var title: String,
    var rating: Int,
    var timestamp: LocalDateTime
) {

    companion object {

        fun fromRating(rating: Rating): HistoryMovie {
            val movie = rating.movie
            val ratingValue = if (rating is Rating.Like) 1 else 0
            return HistoryMovie(movie.id, movie.title, ratingValue, LocalDateTime.now())
        }

        fun fromWatchlistMovie(movie: WatchlistMovieViewEntity, rating: Int): HistoryMovie {
            return HistoryMovie(movie.id, movie.title, rating, LocalDateTime.now())
        }

    }

}
