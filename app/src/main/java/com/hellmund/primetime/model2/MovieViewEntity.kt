package com.hellmund.primetime.model2

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MovieViewEntity(
        val id: Int,
        val posterUrl: String,
        val title: String,
        val formattedGenres: String,
        val description: String,
        val releaseYear: String,
        val popularity: Float,
        val formattedVoteAverage: String,
        val runtime: Int? = null,
        val imdbId: String? = null,
        val raw: ApiMovie
): Parcelable {

    val hasAdditionalInformation: Boolean
        get() {
            return runtime != null && imdbId != null
        }

}
