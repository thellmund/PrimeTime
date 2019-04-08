package com.hellmund.primetime.main;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hellmund.primetime.R;
import com.hellmund.primetime.api.ApiClient;
import com.hellmund.primetime.database.PrimeTimeDatabase;
import com.hellmund.primetime.history.HistoryRepository;
import com.hellmund.primetime.model2.ApiMovie;
import com.hellmund.primetime.utils.Constants;
import com.hellmund.primetime.utils.DeviceUtils;
import com.hellmund.primetime.utils.Dialogs;
import com.hellmund.primetime.utils.DownloadUtils;
import com.hellmund.primetime.utils.GenresProvider;
import com.hellmund.primetime.utils.RealGenresProvider;
import com.hellmund.primetime.utils.UiUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

public class SuggestionFragment extends Fragment {

    // private static final String LOG_TAG = "SuggestionFragment";

    private static final String KEY_MOVIE = "KEY_MOVIE";
    private static final int DEFAULT_LINES = 2;

    private boolean isOverlayExpanded;

    private Unbinder mUnbinder;
    // private OnInteractionListener mCallback;

    @BindView(R.id.background) ImageView mBackground;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;

    @BindView(R.id.overlay) LinearLayout mOverlay;
    @BindView(R.id.movie_title) TextView mTitleView;
    @BindView(R.id.movie_description) TextView mDescriptionView;
    @BindView(R.id.show_more_icon) ImageView mShowMoreIcon;
    @BindView(R.id.show_more_text) TextView mShowMoreText;

    @BindView(R.id.genres_container) LinearLayout mGenresContainer;
    @BindView(R.id.genres) TextView mGenresView;

    @BindView(R.id.infos_container) LinearLayout mInfosContainer;
    @BindView(R.id.rating) TextView mRatingView;
    @BindView(R.id.runtime) TextView mRuntimeView;
    @BindView(R.id.release) TextView mReleaseView;

    @BindView(R.id.action_buttons) LinearLayout mActionButtons;
    @BindView(R.id.movie_add_watchlist_button) AppCompatButton mWatchlistButton;

    // private SuggestionFragmentPresenter mPresenter;
    private ApiMovie movie;

    private SuggestionsViewModel viewModel;

    private ViewPagerHost viewPagerHost;

    public static SuggestionFragment newInstance(ViewPagerHost viewPagerHost, ApiMovie movie) {
        SuggestionFragment fragment = new SuggestionFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_MOVIE, movie);
        fragment.setArguments(args);
        fragment.viewPagerHost = viewPagerHost;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (getArguments() != null) {
            movie = getArguments().getParcelable(KEY_MOVIE);
        }

        isOverlayExpanded = savedInstanceState != null && savedInstanceState.getBoolean("isOverlayExpanded");

        GenresProvider provider = new RealGenresProvider(PreferenceManager.getDefaultSharedPreferences(requireContext()));
        RecommendationsRepository repository = new RecommendationsRepository(ApiClient.getInstance(), provider);
        HistoryRepository historyRepository = new HistoryRepository(PrimeTimeDatabase.getInstance(requireContext()));
        SuggestionsViewModel.Factory factory = new SuggestionsViewModel.Factory(repository, historyRepository, movie);

        viewModel = ViewModelProviders.of(this, factory).get(SuggestionsViewModel.class);
        viewModel.getViewModelEvents().observe(this, this::handleViewModelEvent);
    }

    private void handleViewModelEvent(ViewModelEvent viewModelEvent) {
        if (viewModelEvent instanceof ViewModelEvent.TrailerLoading) {
            showDialog();
        } else if (viewModelEvent instanceof ViewModelEvent.TrailerLoaded) {
            mDialog.dismiss();
            ViewModelEvent.TrailerLoaded trailerLoaded = (ViewModelEvent.TrailerLoaded) viewModelEvent;
            String url = trailerLoaded.getUrl();
            openChromeCustomTab(url);
        } else if (viewModelEvent instanceof ViewModelEvent.AdditionalInformationLoaded) {
            ViewModelEvent.AdditionalInformationLoaded event = (ViewModelEvent.AdditionalInformationLoaded) viewModelEvent;
            showMovieDetails(event.getMovie());
        } else if (viewModelEvent instanceof ViewModelEvent.ImdbLinkLoaded) {
            ViewModelEvent.ImdbLinkLoaded event = (ViewModelEvent.ImdbLinkLoaded) viewModelEvent;
            openImdb(event.getUrl());
        } else if (viewModelEvent instanceof ViewModelEvent.RatingStored) {
            // ViewModelEvent.RatingStored event = (ViewModelEvent.RatingStored) viewModelEvent;
            viewPagerHost.scrollToNext();
        }
    }

    private void showMovieDetails(ApiMovie movie) {
        if (movie.getRuntime() != null && movie.getRuntime() > 0) {
            mRuntimeView.setText(movie.getPrettyRuntime());
        } else {
            mRuntimeView.setText(R.string.no_information);
        }
    }

    private ProgressDialog mDialog;

    private void showDialog() {
        mDialog = Dialogs.showLoading(requireContext(), R.string.opening_trailer);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_suggestion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);

        /*final int color = ContextCompat.getColor(requireContext(), R.color.colorAccent);
        mProgressBar.getIndeterminateDrawable()
                .setColorFilter(color, android.graphics.PorterDuff.Mode.MULTIPLY);*/

        fillInContent();
        downloadPoster();

        // mPresenter.downloadPoster();
        // TODO toggleAdditionalInformation(isOverlayExpanded);
        updateWatchlistButton();

        /*ViewTreeObserver observer = mBackground.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                centerProgressBar();
                mBackground.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });*/
    }

    private void downloadPoster() {
        final String url = DownloadUtils.getPosterURL(requireContext(), movie.getPosterUrl());
        Glide.with(requireContext())
                .load(url)
                .apply(RequestOptions.centerCropTransform())
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                        displayPoster(bitmap);

                        Palette palette = Palette.from(bitmap).generate();
                        Palette.Swatch swatch = palette.getVibrantSwatch();

                        if (swatch != null) {
                            setWatchlistButton();
                        }
                    }
                });
    }

    /*private void centerProgressBar() {
        final float backgroundHeight = mBackground.getHeight();
        final float overlayHeight = mOverlay.getHeight();
        final float spinnerHeight = mProgressBar.getHeight();

        final float available = backgroundHeight - overlayHeight;
        final float newY = (available - spinnerHeight) / 2;
        mProgressBar.setY(newY);
    }*/

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("isOverlayExpanded", isOverlayExpanded);
        savedInstanceState.putParcelable("movie", movie);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisible) {
        super.setUserVisibleHint(isVisible);

        /*if (isVisible && mCallback != null) {
            updateWatchlistButton();
        }

        if (mPresenter != null && mPresenter.hasAdditionalInformation()) {
            downloadAdditionalInformation();
        }*/
    }

    @OnClick(R.id.rating_button)
    public void openRatingDialog() {
        String[] options = new String[] {
                getString(R.string.show_more_like_this),
                getString(R.string.show_less_like_this)
        };

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.adjust_recommendations))
                .setItems(options, (dialog, which) -> viewModel.handleRating(which))
                .setCancelable(true)
                .show();
    }

    @OnClick({R.id.overlay, R.id.show_more})
    public void toggleAdditionalInformation() {
        isOverlayExpanded = (mInfosContainer.getVisibility() == View.GONE);
        toggleAdditionalInformation(isOverlayExpanded);
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
        mInfosContainer.setVisibility(visibility);
        mActionButtons.setVisibility(visibility);

        final int rotation = showDetails ? 180 : 0;
        mShowMoreIcon.setRotation(rotation);

        final int text = showDetails ? R.string.show_less : R.string.show_more;
        mShowMoreText.setText(text);
    }

    @OnClick(R.id.movie_add_watchlist_button)
    public void addToWatchlist() {
        /*final int watchedStatus = mCallback.onGetWatchedStatus(mPresenter.getPosition());

        if (watchedStatus == Constants.NOT_WATCHED) {
            mCallback.onAddToWatchlist(mPresenter.getPosition());
            setButtonText(Constants.ON_WATCHLIST);
            setButtonColor(Constants.ON_WATCHLIST);
        } else if (watchedStatus == Constants.ON_WATCHLIST) {
            displayRemoveDialog();
        }*/
    }

    private void displayRemoveDialog() {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.remove_from_watchlist)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.remove, (dialog, which) -> {
                    // mCallback.onRemoveFromWatchlist(mPresenter.getMovie().getID());
                    updateWatchlistButton();
                    UiUtils.showToast(getContext(),
                            getContext().getString(R.string.watchlist_removed));
                })
                .show();
    }

    private void updateWatchlistButton() {
        /*final int watchedStatus = mCallback.onGetWatchedStatus(mPresenter.getPosition());
        setButtonText(watchedStatus);

        if (mPresenter.getColor() != 0) {
            setButtonColor(watchedStatus);
        }*/
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
        /*final int darkerColor = mPresenter.getDarkerColor();

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
        }*/
    }

    private void fillInContent() {
        mTitleView.setText(movie.getTitle());
        mDescriptionView.setText(movie.getDescription());
        mGenresView.setText(movie.getPrettyGenres(requireContext()));
        mRatingView.setText(movie.getPrettyVoteAverage());
        mReleaseView.setText(movie.getReleaseYear(requireContext()));

        if (movie.getHasAdditionalInformation()) {
            displayRuntime();
        } else {
            downloadAdditionalInformation();
        }

        /*if (movie instanceof PersonalRecommendation) {
            PersonalRecommendation movie2 = (PersonalRecommendation) movie;

            if (movie2.isNowPlaying() || movie2.isUpcoming() || movie2.hasReferrerName()) {
                displayReferrer();
            } else {
                mPresenter.downloadReferrer(movie2.getReferrerID());
            }
        } else {
            displayReferrer();
        }*/
    }

    @OnClick(R.id.trailer_button)
    public void openTrailer() {
        viewModel.loadTrailer();
    }

    @OnClick(R.id.more_info_button)
    public void openMoreInfo() {
        viewModel.openImdb();
    }

    private void openImdb(String url) {
        if (SDK_INT >= JELLY_BEAN_MR2) {
            openChromeCustomTab(url);
        } else {
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        }
    }

    private void openChromeCustomTab(String url) {
        final int color = ContextCompat.getColor(requireContext(), R.color.colorPrimary);
        final Uri uri = Uri.parse(url);
        new CustomTabsIntent.Builder().setToolbarColor(color).build().launchUrl(requireContext(), uri);
    }

    private void displayRuntime() {
        try {
            // mRuntimeView.setText(mPresenter.getMovie().getPrettyRuntime());
        } catch (IllegalStateException e) {
            // Log.e(LOG_TAG, "Error when displaying additional information", e);
        }
    }

    private void downloadAdditionalInformation() {
        viewModel.loadAdditionalInformation();
        // mPresenter.downloadAdditionalInformation();
    }

    private void displayReferrer() {
        /*final String text =
                String.format(getString(R.string.movie_referrer), mPresenter.getReferrerText());
        mReferrerView.setText(text);*/
    }

    /*public void setReferrerView(String text) {
        mReferrerView.setText(text);
    }*/

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

    interface ViewPagerHost {
        void scrollToNext();
        void scrollToPrevious();
    }

}
