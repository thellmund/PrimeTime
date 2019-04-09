package com.hellmund.primetime.watchlist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hellmund.primetime.R;
import com.hellmund.primetime.database.WatchlistMovie;
import com.hellmund.primetime.utils.DateUtils;
import com.hellmund.primetime.utils.DownloadUtils;
import com.hellmund.primetime.utils.NotificationUtils;
import com.hellmund.primetime.utils.UiUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class WatchlistMovieFragment extends Fragment {

    private static final String KEY_WATCHLIST_MOVIE = "KEY_WATCHLIST_MOVIE";

    private Unbinder mUnbinder;
    private WatchlistMovie mMovie;

    @BindView(R.id.posterImageView) ImageView mImageView;
    @BindView(R.id.notification_icon) ImageView mNotificationIcon;

    @BindView(R.id.title) TextView mTitleTextView;
    @BindView(R.id.runtime_icon) ImageView mRuntimeIcon;
    @BindView(R.id.runtime_text) TextView mRuntimeTextView;
    @BindView(R.id.watched_button) AppCompatButton mWatchedItButton;

    public static WatchlistMovieFragment newInstance(WatchlistMovie movie) {
        WatchlistMovieFragment fragment = new WatchlistMovieFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_WATCHLIST_MOVIE, movie);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (getArguments() != null) {
            mMovie = getArguments().getParcelable(KEY_WATCHLIST_MOVIE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.fragment_watchlist_item, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        String posterUrl = DownloadUtils.getPosterURL(requireContext(), mMovie.getPosterURL());
        Glide.with(requireActivity())
             .load(posterUrl)
             .into(mImageView);

        mTitleTextView.setText(mMovie.getTitle());

        if (mMovie.isUnreleased()) {
            setNotificationIcon();
            setMovieOverlay();
        } else if (!mMovie.getHasRuntime()) {
            // downloadRuntime();
        } else {
            mRuntimeTextView.setText(DateUtils.formatRuntime(mMovie.getRuntime()));
        }

        return view;
    }

    private void setMovieOverlay() {
        String releaseDate = DateUtils.getDateInLocalFormat(mMovie.getReleaseDate());
        String releaseStr = String.format(requireContext().getString(R.string.release_on), releaseDate);
        mRuntimeTextView.setText(releaseStr);
        mRuntimeIcon.setImageResource(R.drawable.ic_today_white_24dp);
        mWatchedItButton.setVisibility(View.GONE);
    }

    private void setNotificationIcon() {
        if (NotificationUtils.areNotificationsEnabled(requireContext())) {
            mNotificationIcon.setVisibility(View.VISIBLE);

            if (mMovie.isNotificationActivited()) {
                mNotificationIcon.setImageResource(R.drawable.ic_notifications_active_white_24dp);
            } else {
                mNotificationIcon.setImageResource(R.drawable.ic_notifications_none_white_24dp);
            }
        } else {
            mNotificationIcon.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.notification_icon)
    public void onNotificationClick() {
        final boolean isActivated = !mMovie.isNotificationActivited();
        final String releaseDate = DateUtils.getDateInLocalFormat(mMovie.getReleaseDate());

        String message;

        if (isActivated) {
            message = getString(R.string.showing_notification_on, releaseDate);
        } else {
            message = getString(R.string.notification_off);
        }

        mMovie.setNotificationsActivated(isActivated);
        // TODO
        //Watchlist.update(mMovie);
        //RealmManager.updateWatchlistMovie(mMovie);
        setNotificationIcon();

        UiUtils.showToast(getActivity(), message);
    }

    @OnClick(R.id.watched_button)
    public void onWatchedIt() {
        // TODO
    }

    @OnClick(R.id.remove_button)
    public void onRemove() {
        // TODO
    }

    /*private void downloadRuntime() {
        getActivity().getSupportLoaderManager().initLoader(mMovie.getID(), null,
                new LoaderManager.LoaderCallbacks<Integer>() {
            @Override
            public Loader<Integer> onCreateLoader(int id, Bundle args) {
                return new DownloadRuntimeLoader(getActivity(), mMovie.getID());
            }

            @Override
            public void onLoadFinished(Loader<Integer> loader, Integer runtime) {
                if (runtime != null && runtime > 0) {
                    mMovie.setRuntime(runtime);
                    //Watchlist.update(mMovie);
                    //RealmManager.updateWatchlistMovie(mMovie);
                    mRuntimeTextView.setText(DateUtils.formatRuntime(runtime));
                } else {
                    mRuntimeTextView.setText(mContext.getString(R.string.not_available));
                }
            }

            @Override
            public void onLoaderReset(Loader<Integer> loader) {}
        });
    }*/

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    /*private static class DownloadRuntimeLoader extends AsyncTaskLoader<Integer> {

        private Integer mResult;
        private int mId;

        DownloadRuntimeLoader(Context context, int id) {
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
        public Integer loadInBackground() {
            return DownloadManager.downloadRuntime(mId);
        }

        @Override
        public void deliverResult(Integer result) {
            mResult = result;
            super.deliverResult(result);
        }

    }*/

    /*
    private static class DownloadRuntimeTask extends AsyncTask<String, Void, String> {

        private WeakReference<Context> contextRef;
        private WeakReference<WatchlistMovie> movieRef;
        private WeakReference<TextView> runtimeViewRef;

        DownloadRuntimeTask(Context context, WatchlistMovie movie, TextView runtimeView) {
            this.contextRef = new WeakReference<>(context);
            this.movieRef = new WeakReference<>(movie);
            this.runtimeViewRef = new WeakReference<>(runtimeView);
        }

        @Override
        protected String doInBackground(String... params) {
            WatchlistMovie movie = movieRef.get();

            if (movie != null) {
                final int runtime = DownloadManager.downloadRuntime(movie.getID());

                if (runtime > 0) {
                    movie.setRuntime(formatRuntime(runtime));
                    RealmManager.updateWatchlistMovie(movie);
                }

                return formatRuntime(runtime);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Context context = contextRef.get();
            TextView runtimeView = runtimeViewRef.get();

            if (context == null || runtimeView == null) {
                return;
            }

            if (result == null) {
                runtimeView.setText(context.getString(R.string.not_available));
            } else {
                runtimeView.setText(result);
            }
        }
    }
    */

    public interface OnInteractionListener {
        void onWatchedIt(int position);
        void onRemove(int position);
        WatchlistMovie onGetMovie(int position);
    }

}
