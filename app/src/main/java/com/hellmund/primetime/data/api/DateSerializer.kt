package com.hellmund.primetime.data.api

import android.util.Log
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import org.threeten.bp.LocalDate
import java.lang.reflect.Type

class DateSerializer : JsonDeserializer<LocalDate?> {

    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
    ): LocalDate? {
        return try {
            LocalDate.parse(json?.asString)
        } catch (e: Exception) {
            Log.i("DateSerializer", "Parsing date ${json?.asString} did not work", e)
            null
        }
    }

}
