package com.hellmund.primetime.main

import android.os.Parcelable
import com.hellmund.primetime.model.ApiGenre
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

    /*
    companion object {

        @JvmStatic
        fun fromIntent(context: Context, intent: String): RecommendationsType {
            return when (intent) {
                Constants.NOW_PLAYING_INTENT -> NowPlaying
                Constants.UPCOMING_INTENT -> Upcoming
                else -> {
                    // TODO
                    val database = PrimeTimeDatabase.getInstance(context)
                    val genre = database.genreDao().getGenre(intent).blockingGet()
                    val apiGenre = ApiGenre(genre.id, genre.name)
                    ByGenre(apiGenre)
                }
            }
        }

    }
    */

}