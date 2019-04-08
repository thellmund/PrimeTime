package com.hellmund.primetime.search;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v7.app.AppCompatActivity;

import com.hellmund.primetime.R;
import com.hellmund.primetime.model.SearchResult;
import com.hellmund.primetime.utils.DownloadManager;

import java.util.ArrayList;
import java.util.Date;

public class SearchActivity extends AppCompatActivity {

    private static final String LOG_TAG = "SearchActivity";

    public static final int DISPLAY_LIST = 1;     /* List with query genres */
    public static final int DISPLAY_LOADING = 2;  /* Loading indicator */
    public static final int DISPLAY_EMPTY = 3;    /* Placeholder with no genres */

    public static final float ENABLED = 1.0f;
    public static final float DISABLED = 0.7f;

    /*
    @BindView(R.id.search_box)
    EditText mSearchBar;
    @BindView(R.id.search_clear)
    ImageView mClearButton;
    @BindView(R.id.content_container)
    FrameLayout mContent;

    @BindView(R.id.results_list)
    ListView mListView;
    @BindView(R.id.placeholder_container)
    LinearLayout mPlaceholder;
    @BindView(R.id.loading_container) LinearLayout mLoadingIndicator;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contentFrame, SearchFragment.newInstance(null))
                    .commit();
        }

        /*getWindow().setBackgroundDrawable(null);
        ButterKnife.bind(this);

        initToolbar();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }*/
    }

    /*@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        ArrayList<SearchResult> genres = null;
        int topItem = 0;
        int topPadding = 0;

        if (mListView.getAdapter() != null) {
            genres = ((SearchAdapter) mListView.getAdapter()).getItems();
            topItem = mListView.getFirstVisiblePosition();
            View v = mListView.getChildAt(0);
            topPadding = (v == null) ? 0 : (v.getTop() - mListView.getPaddingTop());
        }

        savedInstanceState.putInt("viewState", getViewState());
        savedInstanceState.putParcelableArrayList("genres", genres);
        savedInstanceState.putInt("topItem", topItem);
        savedInstanceState.putInt("topPadding", topPadding);
        super.onSaveInstanceState(savedInstanceState);
    }

    private int getViewState() {
        if (mLoadingIndicator.getVisibility() == View.VISIBLE) {
            return DISPLAY_LOADING;
        } else if (mPlaceholder.getVisibility() == View.VISIBLE) {
            return DISPLAY_EMPTY;
        } else {
            return DISPLAY_LIST;
        }
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        final int viewState = savedInstanceState.getInt("viewState");
        ArrayList<SearchResult> genres = savedInstanceState.getParcelableArrayList("genres");

        final int topItem = savedInstanceState.getInt("topItem");
        final int topPadding = savedInstanceState.getInt("topPadding");

        if (genres != null && !genres.isEmpty()) {
            SearchAdapter adapter = new SearchAdapter(SearchActivity.this, genres);
            mListView.setAdapter(adapter);
            mListView.setSelectionFromTop(topItem, topPadding);
        }

        toggleViews(viewState);
    }

    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @OnEditorAction(R.id.search_box)
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            final String input = v.getText().toString().trim();
            if (input.isEmpty()) {
                mSearchBar.getText().clear();
                mSearchBar.requestFocus();
            } else {
                toggleKeyboard(false);
                downloadQueryResults(input);
            }

            return true;
        }

        return false;
    }

    @OnTextChanged(R.id.search_box)
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        final float alpha = (s.toString().isEmpty()) ? DISABLED : ENABLED;
        mClearButton.setAlpha(alpha);
    }

    @OnClick(R.id.search_clear)
    public void clearSearch(View view) {
        if (view.getAlpha() == Constants.ENABLED) {
            clearSearchBarContent();
            toggleKeyboard(true);
        }
    }

    private void clearSearchBarContent() {
        mClearButton.setAlpha(DISABLED);
        mSearchBar.getText().clear();
    }

    @OnItemClick(R.id.results_list)
    void onItemClick(int position) {
        showSimilarMovies(position);
    }

    @OnItemLongClick(R.id.results_list)
    boolean onItemLongClick(int position) {
        displayRatingDialog(position);
        return true;
    }

    private void displayRatingDialog(final int position) {
        final SearchResult result = (SearchResult) mListView.getAdapter().getItem(position);
        final String title = result.getTitle();

        final ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.show_more_like_this));
        options.add(getString(R.string.show_less_like_this));

        *//*
        if (!Watchlist.contains(result.getID()) && !History.contains(result.getID())) {
            options.add(getString(R.string.add_to_watchlist));
        }
        *//*

        final String[] items = options.toArray(new String[options.size()]);

        new AlertDialog.Builder(SearchActivity.this)
                .setTitle(title)
                .setItems(items, (dialog, which) -> {
                    if (which == 2) {
                        addToWatchlist(result);
                    } else {
                        final int rating = getRating(which);
                        addRating(position, rating);
                    }
                })
                .setCancelable(true)
                .show();
    }

    private void addToWatchlist(final SearchResult searchResult) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding movie to watchlist ...");
        progressDialog.show();

        getSupportLoaderManager().initLoader(1, null, new LoaderManager.LoaderCallbacks<Long[]>() {
            @Override
            public Loader<Long[]> onCreateLoader(int id, Bundle args) {
                return new DownloadRuntimeReleaseLoader(SearchActivity.this, searchResult.getID());
            }

            @Override
            public void onLoadFinished(Loader<Long[]> loader, Long[] genres) {
                if (genres[1] != null) {
                    final Date releaseDate = new Date(genres[1]);
                    searchResult.setReleaseDate(releaseDate);
                }

                if (genres[0] != null) {
                    final int runtime = genres[0].intValue();
                    searchResult.setRuntime(runtime);
                    //Watchlist.add(searchResult);
                    progressDialog.dismiss();
                    UiUtils.showToast(getApplicationContext(), R.string.added_to_watchlist);
                }
            }

            @Override
            public void onLoaderReset(Loader<Long[]> loader) {}
        });
    }

    private int getRating(int which) {
        return (which == 0) ? Constants.LIKE : Constants.DISLIKE;
    }

    private void addRating(final int position, final int rating) {
        String message;

        if (rating == 1) {
            message = getString(R.string.will_more_like_this);
        } else {
            message = getString(R.string.will_less_like_this);
        }

        final SearchResult result =
                (SearchResult) mListView.getAdapter().getItem(position);
        //History.add(result, rating);

        Snackbar.make(mListView, message, Snackbar.LENGTH_LONG)
                //.setAction(R.string.undo, v -> History.remove(result.getID()))
                .setActionTextColor(UiUtils.getSnackbarColor(this))
                .show();
    }

    private void toggleKeyboard(boolean show) {
        mSearchBar.requestFocus();
        final InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (show) {
            inputMethodManager.showSoftInput(mSearchBar, InputMethodManager.SHOW_IMPLICIT);
        } else {
            inputMethodManager.hideSoftInputFromWindow(mSearchBar.getWindowToken(), 0);
        }
    }

    private void toggleViews(int state) {
        mListView.setVisibility(View.GONE);
        mPlaceholder.setVisibility(View.GONE);
        mLoadingIndicator.setVisibility(View.GONE);

        switch (state) {
            case DISPLAY_LIST:
                mListView.setVisibility(View.VISIBLE);
                break;
            case DISPLAY_LOADING:
                mLoadingIndicator.setVisibility(View.VISIBLE);
                break;
            case DISPLAY_EMPTY:
                mPlaceholder.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void showSimilarMovies(int position) {
        SearchResult result = (SearchResult) mListView.getAdapter().getItem(position);
        final int id = result.getID();
        final String title = result.getTitle();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Constants.SINGLE_MOVIE, true);
        intent.putExtra(Constants.MOVIE_ID, id);
        intent.putExtra(Constants.MOVIE_TITLE, title);

        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void downloadQueryResults(final String query) {
        getSupportLoaderManager().destroyLoader(0);
        getSupportLoaderManager().initLoader(0, null,
                new LoaderManager.LoaderCallbacks<ArrayList<SearchResult>>() {
            @Override
            public Loader<ArrayList<SearchResult>> onCreateLoader(int id, Bundle args) {
                return new QueryTaskLoader(SearchActivity.this, query);
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<SearchResult>> loader,
                                       ArrayList<SearchResult> data) {
                if (!data.isEmpty()) {
                    SearchAdapter adapter = new SearchAdapter(SearchActivity.this, data);
                    mListView.setAdapter(adapter);
                    toggleViews(DISPLAY_LIST);
                } else {
                    toggleViews(DISPLAY_EMPTY);
                }
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<SearchResult>> loader) {}
        });
    }*/

    public static class QueryTaskLoader extends AsyncTaskLoader<ArrayList<SearchResult>> {

        private ArrayList<SearchResult> mResults;
        private String mQuery;

        QueryTaskLoader(Context context, String query) {
            super(context);
            this.mQuery = query;
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
        public ArrayList<SearchResult> loadInBackground() {
            return DownloadManager.downloadSearchResults(mQuery);
        }

        @Override
        public void deliverResult(ArrayList<SearchResult> results) {
            mResults = results;
            super.deliverResult(results);
        }

    }

    public static class DownloadRuntimeReleaseLoader extends AsyncTaskLoader<Long[]> {

        private Long[] mResults;
        private int mId;

        public DownloadRuntimeReleaseLoader(Context context, int id) {
            super(context);
            this.mId = id;
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
        public Long[] loadInBackground() {
            Long[] results = new Long[2];
            results[0] = (long) DownloadManager.downloadRuntime(mId);

            Date countryReleaseDate =
                    DownloadManager.downloadCountryReleaseDate(getContext(), mId);
            if (countryReleaseDate != null) {
                results[1] = countryReleaseDate.getTime();
            }

            return results;
        }

        @Override
        public void deliverResult(Long[] results) {
            mResults = results;
            super.deliverResult(results);
        }

    }

}
