package com.hellmund.primetime.utils

import android.content.Context
import com.hellmund.primetime.R
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.ui.selectgenres.GenresRepository
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.*
import javax.inject.Inject

interface ValueFormatter {
    fun formatGenres(movie: Movie): String
    fun formatReleaseYear(releaseDate: LocalDate?): String
    fun formatRuntime(runtime: Int?): String
    fun formatDate(date: LocalDate): String
    fun formatRating(rating: Int): String
}

class RealValueFormatter @Inject constructor(
        private val context: Context,
        private val genresRepository: GenresRepository
) : ValueFormatter {

    private val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    override fun formatGenres(movie: Movie): String {
        val genreIds = if (movie.genres.isNullOrEmpty()) {
            movie.genreIds.orEmpty()
        } else {
            movie.genres.map { it.id }
        }

        val genres = genresRepository.all.blockingGet().filter { genreIds.contains(it.id) }
        return genres.map { it.name }.sorted().joinToString(", ")
    }

    override fun formatReleaseYear(releaseDate: LocalDate?): String {
        if (releaseDate == null) {
            return context.getString(R.string.no_information)
        }

        return if (releaseDate.isAfterNow) {
            formatter.format(releaseDate)
        } else {
            releaseDate.year.toString()
        }
    }

    override fun formatRuntime(runtime: Int?): String {
        runtime ?: return context.getString(R.string.no_information)

        val hours = String.format(Locale.getDefault(), "%01d", runtime / 60)
        val minutes = String.format(Locale.getDefault(), "%02d", runtime % 60)
        return String.format("%s:%s", hours, minutes)
    }

    override fun formatDate(date: LocalDate): String = formatter.format(date)

    override fun formatRating(rating: Int): String {
        val resId = if (rating == Constants.LIKE) R.string.liked else R.string.disliked
        return context.getString(resId)
    }

}
