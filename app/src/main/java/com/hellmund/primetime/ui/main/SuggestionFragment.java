package com.hellmund.primetime.ui.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hellmund.primetime.R;
import com.hellmund.primetime.model.Movie;
import com.hellmund.primetime.model.PersonalRecommendation;
import com.hellmund.primetime.utils.Constants;
import com.hellmund.primetime.utils.DeviceUtils;
import com.hellmund.primetime.utils.DownloadUtils;
import com.hellmund.primetime.utils.UiUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.hellmund.primetime.R.id.referrer;

public class SuggestionFragment extends Fragment {

    private static final String LOG_TAG = "SuggestionFragment";
    private static final int DEFAULT_LINES = 2;

    private boolean mIsOverlayExpanded;

    private Unbinder mUnbinder;
    private OnInteractionListener mCallback;

    @BindView(R.id.background) ImageView mBackground;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;

    @BindView(R.id.overlay) LinearLayout mOverlay;
    @BindView(R.id.movie_title) TextView mTitleView;
    @BindView(R.id.movie_description) TextView mDescriptionView;
    @BindView(R.id.show_more_icon) ImageView mShowMoreIcon;
    @BindView(R.id.show_more_text) TextView mShowMoreText;

    @BindView(R.id.genres_container) LinearLayout mGenresContainer;
    @BindView(R.id.genres) TextView mGenresView;
    @BindView(R.id.referrer_container) LinearLayout mReferrerContainer;
    @BindView(referrer) TextView mReferrerView;

    @BindView(R.id.infos_container) LinearLayout mInfosContainer;
    @BindView(R.id.rating) TextView mRatingView;
    @BindView(R.id.runtime) TextView mRuntimeView;
    @BindView(R.id.release) TextView mReleaseView;

    @BindView(R.id.action_buttons) LinearLayout mActionButtons;
    @BindView(R.id.movie_add_watchlist_button) AppCompatButton mWatchlistButton;

    private SuggestionFragmentPresenter mPresenter;

    public static SuggestionFragment newInstance(int position, OnInteractionListener listener) {
        SuggestionFragment fragment = new SuggestionFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.POS, position);
        fragment.setArguments(args);
        fragment.mCallback = listener;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Called onCreate() for SuggestionFragment");
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);

        mPresenter = new SuggestionFragmentPresenter(this);
        final int position = getArguments().getInt(Constants.POS);

        Movie movie;

        if (mCallback == null) {
            movie = savedInstanceState.getParcelable("movie");
        } else {
            movie = mCallback.onGetRecommendation(position);
        }

        mPresenter.setPosition(position);
        mPresenter.setMovie(movie);

        mIsOverlayExpanded = savedInstanceState != null
                && savedInstanceState.getBoolean("isOverlayExpanded");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Called onCreateView() for SuggestionFragment");

        View view = inflater.inflate(R.layout.fragment_movie_suggestion, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        final int color = ContextCompat.getColor(requireContext(), R.color.colorAccent);
        mProgressBar.getIndeterminateDrawable()
                    .setColorFilter(color, android.graphics.PorterDuff.Mode.MULTIPLY);

        fillInContent();
        mPresenter.downloadPoster();
        toggleAdditionalInformation(mIsOverlayExpanded);
        updateWatchlistButton();

        ViewTreeObserver observer = mBackground.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                centerProgressBar();
                mBackground.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        return view;
    }

    private void centerProgressBar() {
        final float backgroundHeight = mBackground.getHeight();
        final float overlayHeight = mOverlay.getHeight();
        final float spinnerHeight = mProgressBar.getHeight();

        final float available = backgroundHeight - overlayHeight;
        final float newY = (available - spinnerHeight) / 2;
        mProgressBar.setY(newY);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("isOverlayExpanded", mIsOverlayExpanded);
        savedInstanceState.putParcelable("movie", mPresenter.getMovie());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisible) {
        super.setUserVisibleHint(isVisible);

        if (isVisible && mCallback != null) {
            updateWatchlistButton();
        }

        if (mPresenter != null && mPresenter.hasAdditionalInformation()) {
            downloadAdditionalInformation();
        }
    }

    @OnClick(R.id.rating_button)
    public void openRatingDialog() {
        mCallback.onOpenRatingDialog(mPresenter.getPosition());
    }

    @OnClick({R.id.overlay, R.id.show_more})
    public void toggleAdditionalInformation() {
        mIsOverlayExpanded = (mInfosContainer.getVisibility() == View.GONE);
        toggleAdditionalInformation(mIsOverlayExpanded);
    }

    private void toggleAdditionalInformation(boolean showDetails) {
        final boolean isLandscape = DeviceUtils.isLandscapeMode(getContext());
        final int visibility = (showDetails) ? View.VISIBLE : View.GONE;
        int maxLines;

        if (showDetails) {
            maxLines = Integer.MAX_VALUE;
        } else if (isLandscape) {
            maxLines = 0;
        } else {
            maxLines = DEFAULT_LINES;
        }

        mDescriptionView.setMaxLines(maxLines);
        mGenresContainer.setVisibility(visibility);
        mReferrerContainer.setVisibility(visibility);
        mInfosContainer.setVisibility(visibility);
        mActionButtons.setVisibility(visibility);

        final int rotation = showDetails ? 180 : 0;
        mShowMoreIcon.setRotation(rotation);

        final int text = showDetails ? R.string.show_less : R.string.show_more;
        mShowMoreText.setText(text);
    }

    @OnClick(R.id.movie_add_watchlist_button)
    public void addToWatchlist() {
        final int watchedStatus = mCallback.onGetWatchedStatus(mPresenter.getPosition());

        if (watchedStatus == Constants.NOT_WATCHED) {
            mCallback.onAddToWatchlist(mPresenter.getPosition());
            setButtonText(Constants.ON_WATCHLIST);
            setButtonColor(Constants.ON_WATCHLIST);
        } else if (watchedStatus == Constants.ON_WATCHLIST) {
            displayRemoveDialog();
        }
    }

    private void displayRemoveDialog() {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.remove_from_watchlist)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.remove, (dialog, which) -> {
                    mCallback.onRemoveFromWatchlist(mPresenter.getMovie().getID());
                    updateWatchlistButton();
                    UiUtils.showToast(getContext(),
                            getContext().getString(R.string.watchlist_removed));
                })
                .show();
    }

    private void updateWatchlistButton() {
        if (mCallback == null || mPresenter == null) {
            return;
        }

        final int watchedStatus = mCallback.onGetWatchedStatus(mPresenter.getPosition());
        setButtonText(watchedStatus);

        if (mPresenter.getColor() != 0) {
            setButtonColor(watchedStatus);
        }
    }

    private void setButtonText(int watchedStatus) {
        switch (watchedStatus) {
            case Constants.WATCHED:
                mWatchlistButton.setText(R.string.watched_it);
                break;
            case Constants.ON_WATCHLIST:
                mWatchlistButton.setText(R.string.added_to_watchlist);
                break;
            case Constants.NOT_WATCHED:
                if (mWatchlistButton != null) {
                    mWatchlistButton.setText(R.string.add_to_watchlist);
                }
                break;
        }
    }

    private void setButtonColor(int watchedStatus) {
        final int darkerColor = mPresenter.getDarkerColor();

        switch (watchedStatus) {
            case Constants.WATCHED:
            case Constants.ON_WATCHLIST:
                mWatchlistButton.getBackground()
                        .setColorFilter(darkerColor, PorterDuff.Mode.MULTIPLY);
                break;
            case Constants.NOT_WATCHED:
                mWatchlistButton.getBackground()
                        .setColorFilter(mPresenter.getColor(), PorterDuff.Mode.MULTIPLY);
                break;
        }
    }

    private void fillInContent() {
        Movie movie = mPresenter.getMovie();

        mTitleView.setText(movie.getTitle());
        mDescriptionView.setText(movie.getDescription());
        mGenresView.setText(movie.getPrettyGenres(getContext()));
        mRatingView.setText(movie.getPrettyVoteAverage());
        mReleaseView.setText(movie.getReleaseYear(getContext()));

        if (movie.hasAdditionalInformation()) {
            displayRuntime();
        } else {
            downloadAdditionalInformation();
        }

        if (movie instanceof PersonalRecommendation) {
            PersonalRecommendation movie2 = (PersonalRecommendation) movie;

            if (movie2.isNowPlaying() || movie2.isUpcoming() || movie2.hasReferrerName()) {
                displayReferrer();
            } else {
                mPresenter.downloadReferrer(movie2.getReferrerID());
            }
        } else {
            displayReferrer();
        }
    }

    private ProgressDialog mDialog;

    @OnClick(R.id.trailer_button)
    public void openTrailer() {
        mDialog = new ProgressDialog(getContext());
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage(getString(R.string.opening_trailer));
        mDialog.show();

        mPresenter.downloadTrailer();
    }

    public void openTrailer(Uri uri) {
        mDialog.dismiss();
        openChromeCustomTab(uri);
    }

    @OnClick(R.id.more_info_button)
    public void openMoreInfo() {
        Uri uri = Uri.parse(DownloadUtils.getIMDbURL(mPresenter.getMovie().getIMDbID()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            openChromeCustomTab(uri);
        } else {
            final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    private void openChromeCustomTab(Uri uri) {
        final int color = ContextCompat.getColor(getContext(), R.color.colorPrimary);
        new CustomTabsIntent.Builder().setToolbarColor(color).build().launchUrl(getActivity(), uri);
    }

    private void displayRuntime() {
        try {
            mRuntimeView.setText(mPresenter.getMovie().getPrettyRuntime());
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "Error when displaying additional information", e);
        }
    }

    private void downloadAdditionalInformation() {
        mPresenter.downloadAdditionalInformation();
    }

    private void displayReferrer() {
        final String text =
                String.format(getString(R.string.movie_referrer), mPresenter.getReferrerText());
        mReferrerView.setText(text);
    }

    public void setReferrerView(String text) {
        mReferrerView.setText(text);
    }

    public void setRuntimeView(String text) {
        if (mRuntimeView != null) {
            mRuntimeView.setText(text);
        }
    }

    public void displayPoster(Bitmap poster) {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }

        if (mBackground != null) {
            mBackground.setImageBitmap(poster);
        }
    }

    public void setWatchlistButton() {
        if (mWatchlistButton != null) {
            updateWatchlistButton();
        }
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
    }

    public interface OnInteractionListener {
        void onOpenRatingDialog(int position);
        void onAddToWatchlist(int position);
        void onRemoveFromWatchlist(int position);
        Movie onGetRecommendation(int position);
        int onGetWatchedStatus(int position);
    }

}
