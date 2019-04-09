package com.hellmund.primetime.main

import android.content.Context
import android.os.Parcelable
import com.hellmund.primetime.model.ApiGenre
import com.hellmund.primetime.utils.Constants
import com.hellmund.primetime.utils.GenreUtils
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

    companion object {

        @JvmStatic
        fun fromIntent(context: Context, intent: String): RecommendationsType {
            return when (intent) {
                Constants.NOW_PLAYING_INTENT -> NowPlaying
                Constants.UPCOMING_INTENT -> Upcoming
                else -> {
                    val genreId = GenreUtils.getGenreID(context, intent)
                    val genre = ApiGenre(genreId, intent)
                    ByGenre(genre)
                }
            }
        }

    }

}