package com.hellmund.primetime.core

import android.content.Context
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.Rating
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDate.now
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.Locale
import javax.inject.Inject

interface ValueFormatter {
    fun formatGenres(genres: List<Genre>): String
    fun formatReleaseYear(releaseDate: LocalDate?): String
    fun formatRuntime(runtime: Int?): String
    fun formatDate(date: LocalDateTime): String
    fun formatRating(rating: Rating): String
    fun formatCount(count: Int): String
    fun formatDescription(description: String?): String
}

class RealValueFormatter @Inject constructor(
    private val context: Context
) : ValueFormatter {

    private val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    override fun formatGenres(genres: List<Genre>): String {
        return genres.map { it.name }.sorted().joinToString(", ")
    }

    override fun formatReleaseYear(releaseDate: LocalDate?): String {
        if (releaseDate == null) {
            return context.getString(R.string.no_information)
        }

        return if (releaseDate.isAfter(now())) {
            formatter.format(releaseDate)
        } else {
            releaseDate.year.toString()
        }
    }

    override fun formatRuntime(runtime: Int?): String {
        runtime ?: return context.getString(R.string.loading)

        val hours = String.format(Locale.getDefault(), "%01d", runtime / 60)
        val minutes = String.format(Locale.getDefault(), "%02d", runtime % 60)
        return String.format("%s:%s", hours, minutes)
    }

    override fun formatDate(date: LocalDateTime): String = formatter.format(date)

    override fun formatRating(rating: Rating): String {
        val resId = if (rating == Rating.Like) R.string.liked else R.string.disliked
        return context.getString(resId)
    }

    override fun formatCount(count: Int): String {
        return if (count < 1_000) {
            context.resources.getQuantityString(R.plurals.vote_count_format_string, count, count)
        } else {
            val value = count / 1_000
            context.getString(R.string.thousand_votes_format_string, value)
        }
    }

    override fun formatDescription(
        description: String?
    ): String = description ?: context.getString(R.string.no_information)
}
