package com.hellmund.primetime.ui.main

import android.os.Parcelable
import com.hellmund.primetime.data.model.ApiGenre
import kotlinx.android.parcel.Parcelize

sealed class RecommendationsType : Parcelable {

    @Parcelize
    object Personalized : RecommendationsType()

    @Parcelize
    data class BasedOnMovie(val id: Int, val title: String) : RecommendationsType()

    @Parcelize
    object NowPlaying : RecommendationsType()

    @Parcelize
    object Upcoming : RecommendationsType()

    @Parcelize
    data class ByGenre(val genre: ApiGenre) : RecommendationsType()

}