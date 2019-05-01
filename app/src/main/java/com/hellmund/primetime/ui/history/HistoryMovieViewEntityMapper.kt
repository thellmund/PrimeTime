package com.hellmund.primetime.ui.history

import android.content.Context
import com.hellmund.primetime.R
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.utils.ValueFormatter
import io.reactivex.functions.Function
import javax.inject.Inject

class HistoryMovieViewEntityMapper @Inject constructor(
        private val context: Context,
        private val valueFormatter: ValueFormatter
) : Function<List<HistoryMovie>, List<HistoryMovieViewEntity>> {

    override fun apply(movies: List<HistoryMovie>): List<HistoryMovieViewEntity> {
        return movies.map(this::convert)
    }

    private fun convert(movie: HistoryMovie): HistoryMovieViewEntity {
        val formattedDate = valueFormatter.formatDate(movie.timestamp)
        val formattedRating = valueFormatter.formatRating(movie.rating)
        val formattedDetailsText = context.getString(R.string.added_on, formattedRating, formattedDate)

        return HistoryMovieViewEntity(
                movie.id,
                movie.title,
                movie.rating,
                formattedDate,
                formattedDetailsText,
                movie.isUpdating
        )
    }

}
