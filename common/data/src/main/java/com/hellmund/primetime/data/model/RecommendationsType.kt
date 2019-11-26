package com.hellmund.primetime.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

sealed class RecommendationsType : Parcelable {

    @Parcelize
    data class Personalized(
        val genres: @RawValue List<Genre>? = null
    ) : RecommendationsType()

    @Parcelize
    data class BasedOnMovie(val id: Long, val title: String) : RecommendationsType()

    @Parcelize
    object NowPlaying : RecommendationsType()

    @Parcelize
    object Upcoming : RecommendationsType()

    @Parcelize
    data class ByGenre(val genre: @RawValue Genre) : RecommendationsType()
}
