package com.hellmund.primetime.ui.suggestions

import android.os.Parcelable
import com.hellmund.primetime.data.model.ApiGenre
import com.hellmund.primetime.data.model.Genre
import kotlinx.android.parcel.Parcelize

sealed class RecommendationsType : Parcelable {

    @Parcelize
    data class Personalized(
            val genres: List<Genre>? = null
    ) : RecommendationsType()

    @Parcelize
    data class BasedOnMovie(val id: Int, val title: String) : RecommendationsType()

    @Parcelize
    object NowPlaying : RecommendationsType()

    @Parcelize
    object Upcoming : RecommendationsType()

    @Parcelize
    data class ByGenre(val genre: ApiGenre) : RecommendationsType()

}