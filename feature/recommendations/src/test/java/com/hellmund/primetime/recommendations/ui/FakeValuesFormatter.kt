package com.hellmund.primetime.recommendations.ui

import com.hellmund.primetime.core.ValueFormatter
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.Rating
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class FakeValueFormatter : ValueFormatter {

    override fun formatGenres(genres: List<Genre>): String = "Action, Comedy"

    override fun formatReleaseYear(releaseDate: LocalDate?): String = "2019"

    override fun formatRuntime(runtime: Int?): String = "90 mins"

    override fun formatDate(date: LocalDateTime): String = "01/10/2020"

    override fun formatRating(rating: Rating): String = "Liked"

    override fun formatCount(count: Int): String = "1K"
}
