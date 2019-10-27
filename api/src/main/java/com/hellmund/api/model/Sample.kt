package com.hellmund.api.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class SamplesResponse(val results: List<Sample>)

data class Sample(
    val id: Long,
    val title: String,
    @SerializedName("poster_path") val posterPath: String,
    val popularity: Double,
    val releaseDate: Date?,
    val selected: Boolean = false
) {

    val fullPosterUrl: String
        get() = "https://image.tmdb.org/t/p/w500$posterPath"

}
