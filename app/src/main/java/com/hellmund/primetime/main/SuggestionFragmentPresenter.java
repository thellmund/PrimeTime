package com.hellmund.primetime.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.graphics.Palette;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hellmund.primetime.R;
import com.hellmund.primetime.model.Movie;
import com.hellmund.primetime.model.PersonalRecommendation;
import com.hellmund.primetime.utils.DownloadManager;
import com.hellmund.primetime.utils.DownloadUtils;

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

    void downloadReferrer(final int id) {
        mFragment.getActivity().getSupportLoaderManager().destroyLoader(id);
        mFragment.getActivity().getSupportLoaderManager().initLoader(id, null,
                new LoaderManager.LoaderCallbacks<String>() {
            @Override
            public Loader<String> onCreateLoader(int id, Bundle args) {
                return new DownloadReferrerLoader(mContext, id);
            }

            @Override
            public void onLoadFinished(Loader<String> loader, String data) {
                if (data == null) {
                    String referrer = mContext.getString(R.string.not_available);
                    final String text = String.format(
                            mContext.getString(R.string.movie_referrer), referrer);
                    mFragment.setReferrerView(text);
                    return;
                }

                if (mMovie instanceof PersonalRecommendation) {
                    ((PersonalRecommendation) mMovie).setReferrerName(data);
                }

                final String text = String.format(mContext.getString(R.string.movie_referrer), data);
                mFragment.setReferrerView(text);

                Log.d(LOG_TAG, String.format("Successfully downloaded referrer %s for movie %s", data, mMovie.getTitle()));
            }

            @Override
            public void onLoaderReset(Loader<String> loader) {}
        });
    }

    void downloadTrailer() {
        mFragment.getActivity().getSupportLoaderManager().destroyLoader(mMovie.getID());
        mFragment.getActivity().getSupportLoaderManager().initLoader(mMovie.getID(), null,
                new LoaderManager.LoaderCallbacks<String>() {
            @Override
            public Loader<String> onCreateLoader(int id, Bundle args) {
                return new DownloadTrailerLoader(mContext, id);
            }

            @Override
            public void onLoadFinished(Loader<String> loader, String url) {
                Uri uri;

                if (url != null) {
                    uri = Uri.parse(url);
                } else {
                    uri = Uri.parse(DownloadUtils.getYouTubeQueryURL(mMovie.getTitle()));
                }

                mFragment.openTrailer(uri);
            }

            @Override
            public void onLoaderReset(Loader<String> loader) {}
        });
    }

    void downloadAdditionalInformation() {
        mFragment.getActivity().getSupportLoaderManager().destroyLoader(mMovie.getID());
        mFragment.getActivity().getSupportLoaderManager().initLoader(mMovie.getID(), null,
                new LoaderManager.LoaderCallbacks<Movie>() {
            @Override
            public Loader<Movie> onCreateLoader(int id, Bundle args) {
                return new DownloadRuntimeAndImdbLoader(mContext, id);
            }

            @Override
            public void onLoadFinished(Loader<Movie> loader, Movie result) {
                if (result == null) {
                    mFragment.setRuntimeView(mContext.getString(R.string.no_information));
                    return;
                }

                if (result.getRuntime() != null && result.getRuntime() > 0) {
                    mMovie.setRuntime(result.getRuntime());
                }

                mMovie.setIMDbID(result.getIMDbID());

                if (mMovie.getRuntime() != null && mMovie.getRuntime() > 0) {
                    mFragment.setRuntimeView(mMovie.getPrettyRuntime());
                } else {
                    mFragment.setRuntimeView(mContext.getString(R.string.no_information));
                }
            }

            @Override
            public void onLoaderReset(@NonNull Loader<Movie> loader) {}
        });
    }

    void downloadPoster() {
        Log.d(LOG_TAG, String.format("Downloading poster for %s", mMovie.getTitle()));
        final String url = DownloadUtils.getPosterURL(mContext, mMovie.getPosterURL());

        try {
            Glide.with(mContext)
                    .load(url)
                    .apply(RequestOptions.centerCropTransform())
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                            mFragment.displayPoster(bitmap);

                            Palette palette = Palette.from(bitmap).generate();
                            Palette.Swatch swatch = palette.getVibrantSwatch();

                            if (swatch != null) {
                                mColor = swatch.getRgb();
                                mFragment.setWatchlistButton();
                            }
                        }
                    });
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "Error when loading image with Glide", e);
        }
    }

    private static class DownloadReferrerLoader extends AsyncTaskLoader<String> {

        private String mResult;
        private int mId;

        DownloadReferrerLoader(Context context, int id) {
            super(context);
            this.mId = id;
        }

        @Override
        protected void onStartLoading() {
            if (mResult != null) {
                deliverResult(mResult);
            } else {
                forceLoad();
            }
        }

        @Override
        public String loadInBackground() {
            return DownloadManager.downloadTitle(mId);
        }

        @Override
        public void deliverResult(String result) {
            mResult = result;
            super.deliverResult(result);
        }

    }

    private static class DownloadTrailerLoader extends AsyncTaskLoader<String> {

        private String mResult;
        private int mId;

        DownloadTrailerLoader(Context context, int id) {
            super(context);
            this.mId = id;
        }

        @Override
        protected void onStartLoading() {
            if (mResult != null) {
                deliverResult(mResult);
            } else {
                forceLoad();
            }
        }

        @Override
        public String loadInBackground() {
            return DownloadManager.downloadTrailerUrl(mId);
        }

        @Override
        public void deliverResult(String result) {
            mResult = result;
            super.deliverResult(result);
        }

    }

    private static class DownloadRuntimeAndImdbLoader extends AsyncTaskLoader<Movie> {

        private Movie mResult;
        private int mId;

        DownloadRuntimeAndImdbLoader(Context context, int id) {
            super(context);
            this.mId = id;
        }

        @Override
        protected void onStartLoading() {
            if (mResult != null) {
                deliverResult(mResult);
            } else {
                forceLoad();
            }
        }

        @Override
        public Movie loadInBackground() {
            return DownloadManager.downloadMovie(mId);
        }

        @Override
        public void deliverResult(Movie result) {
            mResult = result;
            super.deliverResult(result);
        }

    }

}
