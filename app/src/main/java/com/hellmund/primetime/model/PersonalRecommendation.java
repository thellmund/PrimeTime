package com.hellmund.primetime.model;

import com.hellmund.primetime.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class PersonalRecommendation extends Movie {

    private int referrerID;
    private String referrerName;

    public int getReferrerID() {
        return referrerID;
    }

    public boolean hasReferrerName() {
        return this.referrerName != null;
    }

    public boolean isUpcoming() {
        return this.referrerID == Constants.UPCOMING_RECOMMENDATION;
    }

    public boolean isNowPlaying() {
        return this.referrerID == Constants.NOW_PLAYING_RECOMMENDATION;
    }

    public String getReferrerName() {
        return referrerName;
    }

    public void setReferrerName(String referrerName) {
        this.referrerName = referrerName;
    }

    PersonalRecommendation(JSONObject obj, int referrerId) throws JSONException {
        super(obj);
        this.referrerID = referrerId;
    }

}
