package com.hellmund.primetime.data.database

import androidx.room.TypeConverter
import com.hellmund.primetime.data.model.Rating
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

class Converters {

    companion object {

        @JvmStatic
        @TypeConverter
        fun toLocalDate(value: Long): LocalDate {
            return Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate()
        }

        @JvmStatic
        @TypeConverter
        fun fromDate(date: LocalDate): Long {
            return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        @JvmStatic
        @TypeConverter
        fun toLocalDateTime(value: Long): LocalDateTime {
            return Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDateTime()
        }

        @JvmStatic
        @TypeConverter
        fun fromDateTime(date: LocalDateTime): Long {
            return date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        @JvmStatic
        @TypeConverter
        fun fromRating(rating: Rating): Int {
            return if (rating == Rating.Like) 1 else 0
        }

        @JvmStatic
        @TypeConverter
        fun toRating(value: Int): Rating {
            return if (value == 1) Rating.Like else Rating.Dislike
        }

    }

}
