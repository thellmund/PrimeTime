package com.hellmund.primetime.database

import android.arch.persistence.room.TypeConverter
import java.util.*

class Converters {

    companion object {

        @JvmStatic
        @TypeConverter
        fun toDate(value: Long): Date = Date(value)

        @JvmStatic
        @TypeConverter
        fun fromDate(date: Date): Long = date.time

    }

}
