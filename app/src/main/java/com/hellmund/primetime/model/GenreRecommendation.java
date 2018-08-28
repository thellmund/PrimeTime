package com.hellmund.primetime.model;

import org.json.JSONException;
import org.json.JSONObject;

public class GenreRecommendation extends Movie {

    private int genreReferrerID;

    public int getGenreReferrerID() {
        return genreReferrerID;
    }

    GenreRecommendation(JSONObject obj, int referrerId) throws JSONException {
        super(obj);
        this.genreReferrerID = referrerId;
    }

}
