package com.hellmund.primetime.main;

import android.content.Context;
import android.graphics.Color;

import com.hellmund.primetime.R;
import com.hellmund.primetime.model.Movie;
import com.hellmund.primetime.model.PersonalRecommendation;

class SuggestionFragmentPresenter {

    private static final String LOG_TAG = "SuggestionFragPresenter";

    private Context mContext;
    private SuggestionFragment mFragment;

    private Movie mMovie;
    private int mPosition;
    private int mColor;

    SuggestionFragmentPresenter(SuggestionFragment fragment) {
        mFragment = fragment;
        mContext = fragment.getContext();
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setMovie(Movie movie) {
        mMovie = movie;
    }

    public Movie getMovie() {
        return mMovie;
    }

    boolean hasAdditionalInformation() {
        return mMovie != null && !mMovie.hasAdditionalInformation();
    }

    String getReferrerText() {
        if (mMovie instanceof PersonalRecommendation) {
            PersonalRecommendation movie = (PersonalRecommendation) mMovie;

            if (movie.isUpcoming()) {
                return mContext.getString(R.string.upcoming);
            } else if (movie.isNowPlaying()) {
                return mContext.getString(R.string.now_playing);
            } else {
                return movie.getReferrerName();
            }
        } else {
            return "Lorem ipsum";
            // final int id = ((GenreRecommendation) mMovie).getGenreReferrerID();
            // return GenreUtils.getGenreName(mContext, id);
        }
    }

    public int getColor() {
        return mColor;
    }

    int getDarkerColor() {
        int darkerColor = mColor;

        if (mColor != 0) {
            float[] hsv = new float[3];
            Color.colorToHSV(mColor, hsv);
            hsv[2] *= 0.8f;
            darkerColor = Color.HSVToColor(hsv);
        }

        return darkerColor;
    }

}
