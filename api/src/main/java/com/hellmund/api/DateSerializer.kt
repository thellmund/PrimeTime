package com.hellmund.api

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
            null
        }
    }

}
