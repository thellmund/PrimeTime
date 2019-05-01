package com.hellmund.primetime.ui.watchlist.details;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.hellmund.primetime.App;
import com.hellmund.primetime.R;
import com.hellmund.primetime.ui.watchlist.WatchlistMovieViewEntity;
import com.hellmund.primetime.utils.Dialogs;
import com.hellmund.primetime.utils.ImageLoader;
import com.hellmund.primetime.utils.NotificationUtils;
import com.hellmund.primetime.utils.UiUtils;

import javax.inject.Inject;
import javax.inject.Provider;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class WatchlistMovieFragment extends Fragment {

    private static final String KEY_WATCHLIST_MOVIE = "KEY_WATCHLIST_MOVIE";

    private ImageView mImageView;
    private ImageView mNotificationIcon;

    private TextView mTitleTextView;
    private ImageView mRuntimeIcon;
    private TextView mRuntimeTextView;
    private AppCompatButton mWatchedItButton;
    private AppCompatButton mRemoveButton;

    @Inject
    ImageLoader imageLoader;

    @Inject
    Provider<WatchlistMovieViewModel> viewModelProvider;

    private WatchlistMovieViewModel viewModel;

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
        return inflater.inflate(R.layout.fragment_watchlist_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mImageView = view.findViewById(R.id.posterImageView);

        mNotificationIcon = view.findViewById(R.id.notification_icon);
        mNotificationIcon.setOnClickListener(v -> viewModel.onNotificationClick());

        mTitleTextView = view.findViewById(R.id.title);
        mRuntimeIcon = view.findViewById(R.id.runtime_icon);
        mRuntimeTextView = view.findViewById(R.id.runtime_text);

        mWatchedItButton = view.findViewById(R.id.watched_button);
        mWatchedItButton.setOnClickListener(v -> viewModel.onWatched());

        mRemoveButton = view.findViewById(R.id.remove_button);
        mRemoveButton.setOnClickListener(v -> viewModel.onRemove());
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

    public interface OnInteractionListener {
        void onWatchedIt(@NonNull WatchlistMovieViewEntity movie);
        void onRemove(@NonNull WatchlistMovieViewEntity movie);
    }

}
