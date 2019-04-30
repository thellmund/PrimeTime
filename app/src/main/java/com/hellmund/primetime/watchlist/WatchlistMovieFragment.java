package com.hellmund.primetime.watchlist;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hellmund.primetime.App;
import com.hellmund.primetime.R;
import com.hellmund.primetime.database.WatchlistMovie;
import com.hellmund.primetime.utils.DateUtils;
import com.hellmund.primetime.utils.ImageLoader;
import com.hellmund.primetime.utils.NotificationUtils;
import com.hellmund.primetime.utils.UiUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.disposables.CompositeDisposable;

public class WatchlistMovieFragment extends Fragment {

    private static final String KEY_WATCHLIST_MOVIE = "KEY_WATCHLIST_MOVIE";
    private static final String KEY_POSITION = "KEY_POSITION";

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Unbinder mUnbinder;
    private WatchlistMovie mMovie;
    private int mPosition;

    @BindView(R.id.posterImageView) ImageView mImageView;
    @BindView(R.id.notification_icon) ImageView mNotificationIcon;

    @BindView(R.id.title) TextView mTitleTextView;
    @BindView(R.id.runtime_icon) ImageView mRuntimeIcon;
    @BindView(R.id.runtime_text) TextView mRuntimeTextView;
    @BindView(R.id.watched_button) AppCompatButton mWatchedItButton;

    private OnInteractionListener listener;

    @Inject
    WatchlistRepository watchlistRepository;

    public static WatchlistMovieFragment newInstance(WatchlistMovie movie, int position,
                                                     OnInteractionListener listener) {
        WatchlistMovieFragment fragment = new WatchlistMovieFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_WATCHLIST_MOVIE, movie);
        args.putInt(KEY_POSITION, position);
        fragment.setArguments(args);
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((App) (context.getApplicationContext())).getAppComponent()
                .inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (getArguments() != null) {
            mMovie = getArguments().getParcelable(KEY_WATCHLIST_MOVIE);
            mPosition = getArguments().getInt(KEY_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.fragment_watchlist_item, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        String posterUrl = mMovie.getFullPosterUrl();
        ImageLoader.with(requireContext()).load(posterUrl, mImageView);

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

        UiUtils.showToast(requireContext(), message);
    }

    @OnClick(R.id.watched_button)
    public void onWatchedIt() {
        // TODO
    }

    @OnClick(R.id.remove_button)
    public void onRemove() {
        listener.onRemove(mMovie, mPosition);
        /*Disposable disposable = repository
                .remove(mMovie.getId())
                .subscribe(
                        () -> listener.onRemove(mMovie, mPosition),
                        t -> Log.e("WatchlistMovieFragment", "Some error", t)
                );
        compositeDisposable.add(disposable);*/
    }

    @Override
    public void onDestroyView() {
        compositeDisposable.clear();
        mUnbinder.unbind();
        super.onDestroyView();
    }

    public interface OnInteractionListener {
        void onWatchedIt(int position);
        void onRemove(WatchlistMovie movie, int position);
        WatchlistMovie onGetMovie(int position);
    }

}
