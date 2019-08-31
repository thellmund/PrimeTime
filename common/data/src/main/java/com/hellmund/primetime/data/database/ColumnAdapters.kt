package com.hellmund.primetime.data.database

import com.hellmund.primetime.data.model.Rating
import com.squareup.sqldelight.ColumnAdapter
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import javax.inject.Inject

class TimestampColumnAdapter @Inject constructor() : ColumnAdapter<LocalDateTime, Long> {

    override fun encode(
        value: LocalDateTime
    ): Long = value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    override fun decode(
        databaseValue: Long
    ): LocalDateTime = Instant
        .ofEpochMilli(databaseValue)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

}

class DateColumnAdapter @Inject constructor() : ColumnAdapter<LocalDate, Long> {

    override fun encode(
        value: LocalDate
    ): Long = value.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    override fun decode(
        databaseValue: Long
    ): LocalDate = Instant.ofEpochMilli(databaseValue).atZone(ZoneId.systemDefault()).toLocalDate()

}

class RatingColumnAdapter @Inject constructor() : ColumnAdapter<Rating, Long> {

    override fun encode(value: Rating): Long = if (value == Rating.Like) 1 else 0

    override fun decode(
        databaseValue: Long
    ): Rating = if (databaseValue == 1L) Rating.Like else Rating.Dislike

}
