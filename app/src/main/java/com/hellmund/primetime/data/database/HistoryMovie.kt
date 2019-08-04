package com.hellmund.primetime.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hellmund.primetime.data.model.Rating
import com.hellmund.primetime.ui.suggestions.RatedMovie
import com.hellmund.primetime.ui.watchlist.RatedWatchlistMovie
import org.threeten.bp.LocalDateTime

@Entity(tableName = "history_movies")
data class HistoryMovie(
    @PrimaryKey var id: Int,
    var title: String,
    var rating: Rating,
    var timestamp: LocalDateTime
) {

    companion object {

        fun from(ratedMovie: RatedMovie): HistoryMovie {
            val movie = ratedMovie.movie
            return HistoryMovie(movie.id, movie.title, ratedMovie.rating, LocalDateTime.now())
        }

        fun from(ratedMovie: RatedWatchlistMovie): HistoryMovie {
            return HistoryMovie(
                ratedMovie.movie.id,
                ratedMovie.movie.title,
                ratedMovie.rating,
                LocalDateTime.now()
            )
        }

    }

}
