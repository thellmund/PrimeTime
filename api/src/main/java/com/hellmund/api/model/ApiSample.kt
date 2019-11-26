package com.hellmund.api.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class SamplesResponse(val results: List<ApiSample>)

data class ApiSample(
    val id: Long,
    val title: String,
    @SerializedName("poster_path") val posterPath: String,
    val popularity: Double,
    val releaseDate: Date?
)
