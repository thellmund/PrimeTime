package com.hellmund.primetime.model2

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Movie(
        val id: Int,
        val posterUrl: String,
        val title: String,
        val genreIds: List<Int>,
        val description: String,
        val releaseDate: Date,
        val popularity: Double,
        val voteAverage: Int,
        val runtime: Int? = null,
        val imdbId: String? = null
): Parcelable
