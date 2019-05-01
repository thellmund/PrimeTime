package com.hellmund.primetime.ui.watchlist.details;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hellmund.primetime.App;
import com.hellmund.primetime.R;
import com.hellmund.primetime.ui.watchlist.WatchlistMovieViewEntity;
import com.hellmund.primetime.utils.Dialogs;
import com.hellmund.primetime.utils.ImageLoader;
import com.hellmund.primetime.utils.NotificationUtils;
import com.hellmund.primetime.utils.UiUtils;

import javax.inject.Inject;
import javax.inject.Provider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class WatchlistMovieFragment extends Fragment {

    private static final String KEY_WATCHLIST_MOVIE = "KEY_WATCHLIST_MOVIE";

    private Unbinder mUnbinder;

    @BindView(R.id.posterImageView) ImageView mImageView;
    @BindView(R.id.notification_icon) ImageView mNotificationIcon;

    @BindView(R.id.title) TextView mTitleTextView;
    @BindView(R.id.runtime_icon) ImageView mRuntimeIcon;
    @BindView(R.id.runtime_text) TextView mRuntimeTextView;
    @BindView(R.id.watched_button) AppCompatButton mWatchedItButton;

    @Inject
    ImageLoader imageLoader;

    @Inject
    Provider<WatchlistMovieViewModel> viewModelProvider;

    WatchlistMovieViewModel viewModel;
    private OnInteractionListener listener;

    public static WatchlistMovieFragment newInstance(WatchlistMovieViewEntity movie,
                                                     OnInteractionListener listener) {
        WatchlistMovieFragment fragment = new WatchlistMovieFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_WATCHLIST_MOVIE, movie);
        fragment.setArguments(args);
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        WatchlistMovieViewEntity movie = getArguments().getParcelable(KEY_WATCHLIST_MOVIE);

        ((App) (context.getApplicationContext())).getAppComponent()
                .watchlistMovieComponent()
                .movie(movie)
                .build()
                .inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        viewModel = viewModelProvider.get();
        viewModel.getViewState().observe(this, this::render);
        viewModel.getMovieEvents().observe(this, this::handleMovieEvent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.fragment_watchlist_item, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    private Dialog removeDialog;

    private void render(WatchlistMovieViewState viewState) {
        WatchlistMovieViewEntity movie = viewState.getMovie();

        String posterUrl = movie.getPosterUrl();
        imageLoader.load(posterUrl, mImageView);

        mTitleTextView.setText(movie.getTitle());

        if (movie.isUnreleased()) {
            setNotificationIcon(movie);
            setMovieOverlay(movie);
        }

        if (movie.getHasRuntime()) {
            mRuntimeTextView.setText(movie.getFormattedRuntime());
        }

        if (viewState.getShowToast()) {
            showToast(movie);
        }

        if (viewState.getShowRemoveDialog()) {
            removeDialog = Dialogs.showCancelable(requireContext(), R.string.remove_from_watchlist_header, R.string.remove, new Function1<DialogInterface, Unit>() {
                @Override
                public Unit invoke(DialogInterface dialogInterface) {
                    viewModel.onConfirmRemove();
                    return Unit.INSTANCE;
                }
            });
        } else {
            if (removeDialog != null) {
                removeDialog.dismiss();
            }
        }
    }

    private void handleMovieEvent(MovieEvent event) {
        if (event instanceof MovieEvent.Remove) {
            MovieEvent.Remove remove = (MovieEvent.Remove) event;
            listener.onRemove(remove.getMovie());
        } else if (event instanceof MovieEvent.MarkWatched) {
            MovieEvent.MarkWatched markWatched = (MovieEvent.MarkWatched) event;
            listener.onWatchedIt(markWatched.getMovie());
        }
    }

    private void showToast(WatchlistMovieViewEntity movie) {
        if (movie.getNotificationsActivated()) {
            UiUtils.showToast(
                    requireContext(),
                    getString(R.string.showing_notification_on, movie.getFormattedReleaseDate())
            );
        } else {
            UiUtils.showToast(requireContext(), R.string.notification_off);
        }
    }

    private void setMovieOverlay(WatchlistMovieViewEntity movie) {
        String releaseDate = movie.getFormattedReleaseDate();
        String releaseStr = String.format(requireContext().getString(R.string.release_on), releaseDate);
        mRuntimeTextView.setText(releaseStr);
        mRuntimeIcon.setImageResource(R.drawable.ic_today_white_24dp);
        mWatchedItButton.setVisibility(View.GONE);
    }

    private void setNotificationIcon(WatchlistMovieViewEntity movie) {
        boolean isAvailable = NotificationUtils.areNotificationsEnabled(requireContext());
        mNotificationIcon.setVisibility(isAvailable ? View.VISIBLE : View.GONE);

        if (movie.getNotificationsActivated()) {
            mNotificationIcon.setImageResource(R.drawable.ic_notifications_active_white_24dp);
        } else {
            mNotificationIcon.setImageResource(R.drawable.ic_notifications_none_white_24dp);
        }
    }

    @OnClick(R.id.notification_icon)
    public void onNotificationClick() {
        viewModel.onNotificationClick();
    }

    @OnClick(R.id.watched_button)
    public void onWatchedIt() {
        viewModel.onWatched();
    }

    @OnClick(R.id.remove_button)
    public void onRemove() {
        viewModel.onRemove();
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
    }

    public interface OnInteractionListener {
        void onWatchedIt(@NonNull WatchlistMovieViewEntity movie);
        void onRemove(@NonNull WatchlistMovieViewEntity movie);
    }

}
