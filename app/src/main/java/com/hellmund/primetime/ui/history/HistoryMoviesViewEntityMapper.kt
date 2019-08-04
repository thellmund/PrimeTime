package com.hellmund.primetime.ui.history

import android.content.Context
import com.hellmund.primetime.R
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.utils.ValueFormatter
import javax.inject.Inject

class HistoryMoviesViewEntityMapper @Inject constructor(
    context: Context,
    valueFormatter: ValueFormatter
) {

    private val internalMapper = HistoryMovieViewEntityMapper(context, valueFormatter)

    operator fun invoke(
        movies: List<HistoryMovie>
    ) = movies.map(internalMapper::invoke)

}

class HistoryMovieViewEntityMapper @Inject constructor(
    private val context: Context,
    private val valueFormatter: ValueFormatter
) {

    operator fun invoke(movie: HistoryMovie) = convert(movie)

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
            movie
        )
    }

}
