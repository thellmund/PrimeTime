package com.hellmund.primetime.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateSerializer : JsonDeserializer<Date?> {

    private val simpleDateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
    ): Date? {
        return try {
            simpleDateFormat.parse(json?.asString)
        } catch (e: ParseException) {
            null
        }
    }

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd"
    }

}
