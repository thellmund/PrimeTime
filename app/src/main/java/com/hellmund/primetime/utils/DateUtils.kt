@file:JvmName("DateUtils")

package com.hellmund.primetime.utils

import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId

val startOfDay: Long
    get() = LocalDate.now()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

val endOfDay: Long
    get() = LocalDate.now()
        .atStartOfDay(ZoneId.systemDefault())
        .withHour(23)
        .withMinute(59)
        .withSecond(59)
        .withNano(999999)
        .toInstant()
        .toEpochMilli()
