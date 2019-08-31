package com.hellmund.primetime.data.model

import org.threeten.bp.LocalDateTime

data class LegacyHistoryMovie(
    var id: Int,
    var title: String,
    var rating: Rating,
    var timestamp: LocalDateTime
)
