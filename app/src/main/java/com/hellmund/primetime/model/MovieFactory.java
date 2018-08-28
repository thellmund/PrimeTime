package com.hellmund.primetime.model;

import com.hellmund.primetime.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class MovieFactory {

    public static Movie from(JSONObject obj, int referrerType, int referrerID) throws JSONException {
        if (referrerType == Constants.MOVIE_REFERRER
                || referrerType == Constants.NOW_PLAYING_REFERRER
                || referrerType == Constants.UPCOMING_REFERRER) {
            return new PersonalRecommendation(obj, referrerID);
        } else {
            return new GenreRecommendation(obj, referrerID);
        }
    }

}
