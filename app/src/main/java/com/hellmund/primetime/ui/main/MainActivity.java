package com.hellmund.primetime.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.hellmund.primetime.R;
import com.hellmund.primetime.model.Genre;
import com.hellmund.primetime.model.Movie;
import com.hellmund.primetime.model.realm.History;
import com.hellmund.primetime.ui.SettingsActivity;
import com.hellmund.primetime.ui.history.HistoryActivity;
import com.hellmund.primetime.ui.search.SearchActivity;
import com.hellmund.primetime.ui.watchlist.WatchlistActivity;
import com.hellmund.primetime.utils.Constants;
import com.hellmund.primetime.utils.DeviceUtils;
import com.hellmund.primetime.utils.GenreUtils;
import com.hellmund.primetime.utils.NotificationUtils;
import com.hellmund.primetime.utils.PrefUtils;
import com.hellmund.primetime.utils.UiUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements MainView, SuggestionFragment.OnInteractionListener,
        SuggestionErrorFragment.OnInteractionListener, DiscoverMoreFragment.OnInteractionListener {

    private static final String LOG_TAG = "MainActivity";

    @BindView(R.id.suggestions) ViewPager mSuggestionsPager;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;

    private MainPresenterImpl mPresenter;
    private int mViewState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setBackgroundDrawable(null);
        ButterKnife.bind(this);

        mPresenter = new MainPresenterImpl(this);
        mPresenter.loadIndices();

        if (getIntent().getExtras() != null) {
            final String intent = getIntent().getStringExtra("intent");
            if (intent != null) {
                mPresenter.handleShortcutOpen(intent);
            }
        }

        initToolbar();
        setToolbarSubtitle();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        if (displaySingleMovieRecommendation()) {
            setupSingleMovieRecommendations();
        }

        if (!PrefUtils.hasDownloadedHistoryInRealm(this) || History.get().isEmpty()) {
            mPresenter.downloadHistoryAndRecommendations();
        } else {
            mPresenter.downloadRecommendationsAsync();
        }

        NotificationUtils.scheduleNotifications(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        MainPresenterImpl presenter = getIntent().getParcelableExtra("presenter");
        if (presenter != null) {
            presenter.restoreState(this);

            ArrayList<Movie> movies = getIntent().getParcelableArrayListExtra("movies");
            if (movies != null) {
                presenter.setRecommendations(movies);
            }

            mPresenter = presenter;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (mPresenter.getRecommendations() != null) {
            savedInstanceState.putParcelableArrayList("movies", mPresenter.getRecommendations());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    public void restoreInstanceState(Bundle savedInstanceState) {
        ArrayList<Movie> movies = savedInstanceState.getParcelableArrayList("movies");
        if (movies != null) {
            mPresenter.setRecommendations(movies);
        }
    }

    private boolean displaySingleMovieRecommendation() {
        final boolean shouldDisplay = getIntent().getBooleanExtra(Constants.SINGLE_MOVIE, false);
        getIntent().removeExtra(Constants.SINGLE_MOVIE);
        return shouldDisplay;
    }

    private void setupSingleMovieRecommendations() {
        mPresenter.setupSingleMovieRecommendations(
                getIntent().getIntExtra(Constants.MOVIE_ID, 0),
                getIntent().getStringExtra(Constants.MOVIE_TITLE)
        );
        setToolbarSubtitle();
    }

    private void initViewPager() {
        int size;

        if (mViewState == Constants.IDEAL_STATE) {
            size = mPresenter.getRecommendations().size() + 1;
        } else {
            size = 1;
        }

        if (mSuggestionsPager != null) {
            SuggestionsAdapter adapter =
                    new SuggestionsAdapter(getSupportFragmentManager(), this, mViewState, size);
            mSuggestionsPager.setAdapter(adapter);
            mProgressBar.setVisibility(View.GONE);
            mSuggestionsPager.setVisibility(View.VISIBLE);
        }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setToolbarSubtitle() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(mPresenter.getToolbarSubtitle());
        }
    }

    private void openHistory() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onMovieRatingAdded(final int id, final int rating) {
        displayRatingSnackbar(mSuggestionsPager.getCurrentItem(), id, rating);
        mSuggestionsPager.setCurrentItem(mSuggestionsPager.getCurrentItem() + 1);
    }

    @Override
    public void tryDownloadAgain() {
        mPresenter.forceRecommendationsDownload();
        mPresenter.downloadRecommendationsAsync();
    }

    private void displayRatingSnackbar(final int position, final int id, final int rating) {
        String message;

        if (rating == Constants.LIKE) {
            message = getString(R.string.will_more_like_this);
        } else {
            message = getString(R.string.will_less_like_this);
        }

        Movie movie = mPresenter.getMovieAt(position);
        //History.add(movie, rating);

        Snackbar.make(mSuggestionsPager, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    mSuggestionsPager.setCurrentItem(position);
                    mPresenter.showUndoToast(id, rating);
                    //History.remove(id);
                })
                .setActionTextColor(UiUtils.getSnackbarColor(this))
                .show();
    }

    private void refreshRecommendations() {
        if (!DeviceUtils.isConnected(this)) {
            UiUtils.showToast(this, getString(R.string.not_connected));
            return;
        }

        mPresenter.forceRecommendationsDownload();
        mPresenter.downloadRecommendationsAsync();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.action_watchlist:
                openWatchlist();
                return true;
            case R.id.action_genre_recommendations:
                openGenresDialog();
                return true;
            case R.id.action_refresh:
                refreshRecommendations();
                return true;
            case R.id.action_history:
                openHistory();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void openWatchlist() {
        Intent intent = new Intent(this, WatchlistActivity.class);
        startActivity(intent);
    }

    @Override
    public void openGenresDialog() {
        final String[] genres = buildGenresList();
        final GenresDialogAdapter adapter = new GenresDialogAdapter(this, genres);

        new AlertDialog.Builder(MainActivity.this)
                .setAdapter(adapter, (dialog, which) -> {
                    final String selected = adapter.getItem(which);

                    if (mPresenter.genreAlreadySelected(selected)) {
                        dialog.dismiss();
                        return;
                    }

                    if (!DeviceUtils.isConnected(MainActivity.this)) {
                        UiUtils.showToast(MainActivity.this,
                                getString(R.string.not_connected));
                    } else {
                        handleGenreDialogInput(selected, which);
                    }
                })
                .setCancelable(true)
                .setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss()).create().show();
    }

    private void handleGenreDialogInput(String selected, int which) {
        if (which == 1) {
            openSearch();
        } else {
            mPresenter.handleGenreDialogInput(selected, which);
            setToolbarSubtitle();
            refreshRecommendations();
        }
    }

    @Override
    public void openSearch() {
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        startActivity(intent);
    }

    private String[] buildGenresList() {
        final int nonGenreCategories = 4;

        final Genre[] genres = GenreUtils.getGenres(this);
        final String[] genreTitles = new String[genres.length];

        for (int i = 0; i < genres.length; i++) {
            genreTitles[i] = genres[i].getName();
        }

        final int length = nonGenreCategories + GenreUtils.getGenres(this).length;

        final String[] categories = new String[length];
        categories[0] = getString(R.string.personalized_recommendations);
        categories[1] = getString(R.string.movie_based_recommendations);
        categories[2] = getString(R.string.now_playing);
        categories[3] = getString(R.string.upcoming);

        System.arraycopy(genreTitles, 0, categories, nonGenreCategories, genreTitles.length);
        return categories;
    }

    @Override
    public void onOpenRatingDialog(final int position) {
        final String[] options = {getString(R.string.show_more_like_this),
                getString(R.string.show_less_like_this)};

        new AlertDialog.Builder(MainActivity.this)
                .setTitle(getString(R.string.adjust_recommendations))
                .setItems(options, (dialog, which) -> {
                    final int rating = (which == 0) ? Constants.LIKE : Constants.DISLIKE;
                    mPresenter.addMovieRating(position, rating);
                })
                .setCancelable(true)
                .show();
    }

    @Override
    public void onAddToWatchlist(int position) {
        mPresenter.addToWatchlist(position);
    }

    @Override
    public void onRemoveFromWatchlist(int id) {
        mPresenter.removeFromWatchlist(id);
    }

    @Override
    public Movie onGetRecommendation(int position) {
        return mPresenter.getMovieAt(position);
    }

    @Override
    public int onGetWatchedStatus(int position) {
        final int id = mPresenter.getMovieAt(position).getID();

        if (mPresenter.onWatchlist(id)) {
            return Constants.ON_WATCHLIST;
        }

        if (PrefUtils.hasDownloadedHistoryInRealm(this)) {
            if (History.contains(id)) {
                return Constants.WATCHED;
            }
        }

        return Constants.NOT_WATCHED;
    }

    @Override
    public void onDownloadStart() {
        if (mSuggestionsPager != null) {
            mSuggestionsPager.setVisibility(View.GONE);
        }

        mProgressBar.setVisibility(View.VISIBLE);
        mSuggestionsPager.setVisibility(View.GONE);
    }

    @Override
    public void onSuccess() {
        mViewState = Constants.IDEAL_STATE;
        initViewPager();
    }

    @Override
    public void onError() {
        mViewState = Constants.ERROR_STATE;
        initViewPager();
    }

    @Override
    public void onEmpty() {
        mViewState = Constants.EMPTY_STATE;
        initViewPager();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        getIntent().putExtra("presenter", mPresenter);
        getIntent().putExtra("movies", mPresenter.getRecommendations());
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.saveIndices();
    }

}