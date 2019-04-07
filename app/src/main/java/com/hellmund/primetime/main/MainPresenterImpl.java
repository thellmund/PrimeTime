package com.hellmund.primetime.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

import com.hellmund.primetime.R;
import com.hellmund.primetime.model.HistoryMovie;
import com.hellmund.primetime.model.Movie;
import com.hellmund.primetime.utils.Constants;
import com.hellmund.primetime.utils.DeviceUtils;
import com.hellmund.primetime.utils.DownloadManager;
import com.hellmund.primetime.utils.DownloadUtils;
import com.hellmund.primetime.utils.GenreUtils;
import com.hellmund.primetime.utils.PrefUtils;
import com.hellmund.primetime.utils.UiUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class MainPresenterImpl implements MainContract.Presenter, Parcelable {

    private ArrayList<Movie> mRecommendations;
    private int mRecommendationsType;
    private int mGenreID;
    private String mMovieTitle;
    private int mMovieID;

    private MainContract.View mView;
    private FragmentActivity mActivity;
    private Context mContext;

    MainPresenterImpl(FragmentActivity activity) {
        this.mActivity = activity;
        this.mContext = activity;
    }

    @Override
    public void attachView(@NotNull MainContract.View view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    private MainPresenterImpl(Parcel in) {
        mRecommendationsType = in.readInt();
        mGenreID = in.readInt();
        mMovieTitle = in.readString();
        mMovieID = in.readInt();
    }

    void restoreState(FragmentActivity activity) {
        this.mContext = activity.getApplicationContext();
        this.mActivity = activity;
        // this.mView = activity;
    }

    public static final Creator<MainPresenterImpl> CREATOR = new Creator<MainPresenterImpl>() {
        @Override
        public MainPresenterImpl createFromParcel(Parcel in) {
            return new MainPresenterImpl(in);
        }

        @Override
        public MainPresenterImpl[] newArray(int size) {
            return new MainPresenterImpl[size];
        }
    };

    @Override
    public void loadIndices() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mRecommendationsType = sharedPrefs.getInt(
                Constants.RECOMM_TYPE, Constants.PERSONALIZED_RECOMMENDATION);

        if (mRecommendationsType == Constants.GENRE_RECOMMENDATION) {
            mGenreID = sharedPrefs.getInt("genre", 28);
        }

        try {
            mMovieID = sharedPrefs.getInt(Constants.MOVIE_ID, 0);
        } catch (ClassCastException e) {
            final String id = sharedPrefs.getString(Constants.MOVIE_ID, "0");
            mMovieID = Integer.parseInt(id);
        }

        mMovieTitle = sharedPrefs.getString(Constants.MOVIE_TITLE, null);
    }

    @Override
    public void handleShortcutOpen(String intent) {
        switch (intent) {
            case Constants.NOW_PLAYING_INTENT:
                mRecommendationsType = Constants.NOW_PLAYING_RECOMMENDATION;
                break;
            case Constants.UPCOMING_INTENT:
                mRecommendationsType = Constants.UPCOMING_RECOMMENDATION;
                break;
            case Constants.WATCHLIST_INTENT:
                mView.openWatchlist();
                break;
            default:
                mView.openSearch();
                break;
        }
    }

    @Override
    public void saveIndices() {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        sharedPrefs.edit().putInt(Constants.RECOMM_TYPE, mRecommendationsType).apply();

        if (mRecommendationsType == Constants.GENRE_RECOMMENDATION) {
            sharedPrefs.edit().putInt("genre", 28).apply();
        }

        sharedPrefs.edit().putInt(Constants.MOVIE_ID, mMovieID).apply();
        sharedPrefs.edit().putString(Constants.MOVIE_TITLE, mMovieTitle).apply();
    }

    @Override
    public String getToolbarSubtitle() {
        if (mRecommendationsType == Constants.PERSONALIZED_RECOMMENDATION) {
            return null;
        } else if (mRecommendationsType == Constants.MOVIE_RECOMMENDATION) {
            return mMovieTitle;
        } else if (mRecommendationsType == Constants.NOW_PLAYING_RECOMMENDATION) {
            return mContext.getString(R.string.now_playing);
        } else if (mRecommendationsType == Constants.UPCOMING_RECOMMENDATION) {
            return mContext.getString(R.string.upcoming);
        } else {
            return GenreUtils.getGenreName(mContext, mGenreID);
        }
    }

    @Override
    public boolean genreAlreadySelected(String selected) {
        if (mRecommendationsType == Constants.PERSONALIZED_RECOMMENDATION) {
            return selected.equals(mContext.getString(R.string.personalized_recommendations));
        } else if (mRecommendationsType == Constants.MOVIE_RECOMMENDATION) {
            return selected.equals(mMovieTitle);
        } else if (mRecommendationsType == Constants.NOW_PLAYING_RECOMMENDATION) {
            return selected.equals(mContext.getString(R.string.now_playing));
        } else if (mRecommendationsType == Constants.UPCOMING_RECOMMENDATION) {
            return selected.equals(mContext.getString(R.string.upcoming));
        } else {
            return selected.equals(GenreUtils.getGenreName(mContext, mGenreID));
        }
    }

    public void handleGenreDialogInput(String selected, int which) {
        if (which == 0) {
            mRecommendationsType = Constants.PERSONALIZED_RECOMMENDATION;
        } else if (which == 2) {
            mRecommendationsType = Constants.NOW_PLAYING_RECOMMENDATION;
        } else if (which == 3) {
            mRecommendationsType = Constants.UPCOMING_RECOMMENDATION;
        } else {
            mRecommendationsType = Constants.GENRE_RECOMMENDATION;
            mGenreID = GenreUtils.getGenreID(mContext, selected);
        }
    }


    @Override
    public void setupSingleMovieRecommendations(int id, String title) {
        this.mRecommendationsType = Constants.MOVIE_RECOMMENDATION;
        this.mMovieID = id;
        this.mMovieTitle = title;
    }

    @Override
    public ArrayList<Movie> getRecommendations() {
        return this.mRecommendations;
    }

    @Override
    public void setRecommendations(ArrayList<Movie> recommendations) {
        this.mRecommendations = recommendations;
    }

    @Override
    public Movie getMovieAt(int position) {
        return mRecommendations.get(position);
    }

    @Override
    public ArrayList<HistoryMovie> getHistory() {
        return new ArrayList<>(); // History.get();
    }

    @Override
    public void addToWatchlist(int position) {
        final Movie movie = mRecommendations.get(position);
        saveInWatchlistOnDevice(movie);
    }

    @Override
    public void saveInWatchlistOnDevice(Movie movie) {
        // Watchlist.add(movie);
    }

    @Override
    public void addMovieRating(int position, int rating) {
        final int id = mRecommendations.get(position).getID();
        removeFromWatchlist(id);
        mView.onMovieRatingAdded(id, rating);
    }

    @Override
    public void removeFromWatchlist(int id) {
        // Watchlist.remove(id);
    }

    @Override
    public void downloadRecommendationsAsync() {
        mView.onDownloadStart();

        mActivity.getSupportLoaderManager().initLoader(Constants.RECOMMENDATIONS_LOADER, null,
                new LoaderManager.LoaderCallbacks<ArrayList<Movie>>() {
                    @Override
                    public Loader<ArrayList<Movie>> onCreateLoader(int id, Bundle args) {
                        if (mRecommendationsType == Constants.PERSONALIZED_RECOMMENDATION
                                || mRecommendationsType == Constants.NOW_PLAYING_RECOMMENDATION
                                || mRecommendationsType == Constants.UPCOMING_RECOMMENDATION) {
                            return new DownloadMoviesLoader(mContext, mRecommendationsType);
                        } else if (mRecommendationsType == Constants.GENRE_RECOMMENDATION) {
                            return new DownloadMoviesLoader(mContext, mRecommendationsType, mGenreID);
                        } else {
                            return new DownloadMoviesLoader(mContext, mRecommendationsType, mMovieID);
                        }
                    }

                    @Override
                    public void onLoadFinished(Loader<ArrayList<Movie>> loader, ArrayList<Movie> data) {
                        mRecommendations = data;

                        if (mRecommendations == null) {
                            mView.onError();
                        } else if (mRecommendations.isEmpty()) {
                            mView.onEmpty();
                        } else {
                            mView.onSuccess();
                        }
                    }

                    @Override
                    public void onLoaderReset(Loader<ArrayList<Movie>> loader) {
                        mRecommendations = null;
                    }
                });
    }

    @Override
    public void forceRecommendationsDownload() {
        mActivity.getSupportLoaderManager().destroyLoader(Constants.RECOMMENDATIONS_LOADER);
    }

    @Override
    public void downloadHistoryAndRecommendations() {
        if (!DeviceUtils.isConnected(mContext)) {
            return;
        }

        mActivity.getSupportLoaderManager().initLoader(Constants.HISTORY_LOADER, null,
                new LoaderManager.LoaderCallbacks<ArrayList<HistoryMovie>>() {
            @Override
            public Loader<ArrayList<HistoryMovie>> onCreateLoader(int id, Bundle args) {
                return new DownloadHistoryTaskLoader(mContext);
            }

            @Override
            public void onLoadFinished(
                    Loader<ArrayList<HistoryMovie>> loader, ArrayList<HistoryMovie> results) {
                // History.addAll(results);
                PrefUtils.setHasDownloadedHistoryInRealm(mContext);
                downloadRecommendationsAsync();
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<HistoryMovie>> loader) {}
        });
    }

    @Override
    public void showUndoToast(int id, int rating) {
        String message;

        if (rating == Constants.LIKE) {
            message = mContext.getString(R.string.removed_like);
        } else {
            message = mContext.getString(R.string.removed_dislike);
        }

        UiUtils.showToast(mContext, message);
    }

    @Override
    public boolean onWatchlist(int id) {
        return false; // Watchlist.contains(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mRecommendationsType);
        dest.writeInt(mGenreID);
        dest.writeString(mMovieTitle);
        dest.writeInt(mMovieID);
    }

    private static class DownloadMoviesLoader extends AsyncTaskLoader<ArrayList<Movie>> {

        private Context context;

        private ArrayList<Movie> results;
        private int recommendationsType;
        private int referrerId;

        DownloadMoviesLoader(Context context, int recommendationsType, int... ids) {
            super(context);
            this.context = context;
            this.recommendationsType = recommendationsType;

            if (ids.length > 0) {
                this.referrerId = ids[0];
            }
        }

        @Override
        protected void onStartLoading() {
            if (results != null) {
                deliverResult(results);
            } else {
                forceLoad();
            }
        }

        @Override
        public ArrayList<Movie> loadInBackground() {
            if (recommendationsType == Constants.PERSONALIZED_RECOMMENDATION) {
                return DownloadManager.downloadPersonalRecommendations(context);
            } else if (recommendationsType == Constants.GENRE_RECOMMENDATION) {
                return DownloadManager.downloadGenreRecommendations(context, referrerId);
            } else if (recommendationsType == Constants.NOW_PLAYING_RECOMMENDATION) {
                return DownloadManager.downloadNowPlayingMovies(context);
            } else if (recommendationsType == Constants.UPCOMING_RECOMMENDATION) {
                return DownloadManager.downloadUpcomingMovies(context);
            } else {
                return DownloadManager.downloadSingleMovieRecommendations(context, referrerId);
            }
        }

        @Override
        public void deliverResult(ArrayList<Movie> data) {
            results = data;
            super.deliverResult(data);
        }

    }

    private static class DownloadHistoryTaskLoader
            extends AsyncTaskLoader<ArrayList<HistoryMovie>> {

        private ArrayList<HistoryMovie> mResults;

        DownloadHistoryTaskLoader(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            if (mResults != null) {
                deliverResult(mResults);
            } else {
                forceLoad();
            }
        }

        @Override
        public ArrayList<HistoryMovie> loadInBackground() {
            final String responseStr = DownloadUtils.getHistoryURL(getContext());

            try {
                JSONArray results = new JSONObject(responseStr).getJSONArray("movies");
                final int length = results.length();
                ArrayList<HistoryMovie> history = new ArrayList<>();

                JSONObject obj;

                for (int i = 0; i < length; i++) {
                    obj = results.getJSONObject(i);
                    history.add(HistoryMovie.fromJSON(obj));
                }

                return history;
            } catch (JSONException e) {
                return null;
            }
        }

        @Override
        public void deliverResult(ArrayList<HistoryMovie> data) {
            mResults = data;
            super.deliverResult(mResults);
        }

    }

}
