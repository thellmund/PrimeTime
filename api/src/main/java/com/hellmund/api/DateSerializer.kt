package com.hellmund.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import javax.inject.Inject
import org.threeten.bp.LocalDate

class DateSerializer @Inject constructor() : JsonDeserializer<LocalDate?> {

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
