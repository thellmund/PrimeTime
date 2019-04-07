package com.hellmund.primetime.model

import com.hellmund.primetime.utils.Constants

import org.json.JSONException
import org.json.JSONObject

object MovieFactory {

    @Throws(JSONException::class)
    @JvmStatic
    fun from(obj: JSONObject, referrerType: Int, referrerID: Int): Movie {
        return if (referrerType == Constants.MOVIE_REFERRER
                || referrerType == Constants.NOW_PLAYING_REFERRER
                || referrerType == Constants.UPCOMING_REFERRER) {
            PersonalRecommendation(obj, referrerID)
        } else {
            GenreRecommendation(obj, referrerID)
        }
    }

}
