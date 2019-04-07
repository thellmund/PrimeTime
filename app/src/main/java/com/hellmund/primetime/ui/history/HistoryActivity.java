package com.hellmund.primetime.ui.history;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hellmund.primetime.R;

public class HistoryActivity extends AppCompatActivity
        /*implements HistoryAdapter.OnInteractionListener*/ {

    /*private static final String LOG_TAG = "HistoryActivity";

    private ArrayList<HistoryMovie> mHistory;

    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.progress_container) LinearLayout mProgressContainer;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        /*ButterKnife.bind(this);
        initToolbar();*/

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contentFrame, HistoryFragment.newInstance())
                    .commit();
        }

        /*mHistory = new ArrayList<>(); // History.get();
        displayHistory();*/
    }

    /*private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void downloadHistory() {
        toggleProgressBar();

        getSupportLoaderManager().initLoader(0, null,
                new LoaderManager.LoaderCallbacks<ArrayList<HistoryMovie>>() {
            @Override
            public Loader<ArrayList<HistoryMovie>> onCreateLoader(int id, Bundle args) {
                return new DownloadHistoryTaskLoader(getApplicationContext());
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<HistoryMovie>> loader,
                                       ArrayList<HistoryMovie> results) {
                if (results != null) {
                    mHistory = results;
                    // History.addAll(results);
                    PrefUtils.setHasDownloadedHistoryInRealm(HistoryActivity.this);
                    displayHistory();
                } else {
                    UiUtils.showToast(HistoryActivity.this,
                            R.string.error_downloading_movies, Toast.LENGTH_LONG);
                    onBackPressed();
                }
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<HistoryMovie>> loader) {
                mHistory = null;
            }
        });
    }

    private void displayHistory() {
        fillListViewContent();
        toggleProgressBar();
    }

    private void toggleProgressBar() {
        if (mProgressContainer.getVisibility() == View.GONE) {
            mRecyclerView.setVisibility(View.GONE);
            mProgressContainer.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mProgressContainer.setVisibility(View.GONE);
        }
    }

    private void fillListViewContent() {
        if (true) {
            return;
        }

        HistoryAdapter adapter = new HistoryAdapter(this, mHistory);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        ItemTouchHelper helper =
                new ItemTouchHelper(new HistoryItemTouchHelper(this, adapter));
        helper.attachToRecyclerView(mRecyclerView);
    }

    private void showSimilarMovies(HistoryMovie movie) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Constants.SINGLE_MOVIE, true);
        intent.putExtra(Constants.MOVIE_ID, movie.getID());
        intent.putExtra(Constants.MOVIE_TITLE, movie.getTitle());
        startActivity(intent);
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

    @Override
    public void onOpenDialog(final int position) {
        final HistoryMovie movie = mHistory.get(position);
        final String[] options = getDialogOptions(position);

        new AlertDialog.Builder(this)
                .setTitle(movie.getTitle())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openEditRatingDialog(position);
                    } else if (which == 1) {
                        showSimilarMovies(movie);
                    }
                }).create().show();
    }

    @Override
    public View getContainer() {
        return mRecyclerView;
    }

    private void openEditRatingDialog(final int position) {
        final HistoryMovie movie = mHistory.get(position);
        String[] options = {getString(R.string.like), getString(R.string.dislike)};
        final int checked = (movie.getRating() == Constants.LIKE) ? 0 : 1;

        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_rating)
                .setSingleChoiceItems(options, checked, null)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    ListView dialogListView = ((AlertDialog) dialog).getListView();
                    final int selected = dialogListView.getCheckedItemPosition();

                    if (selected != checked) {
                        final int newRating = (selected == 0) ? Constants.LIKE : Constants.DISLIKE;
                        updateRating(movie, position, newRating);
                    }

                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }

    private void updateRating(final HistoryMovie movie, final int position, final int newRating) {
        movie.setUpdating(true);
        movie.setRating(newRating);
        mRecyclerView.getAdapter().notifyItemChanged(position);

        new Handler().postDelayed(() -> {
            // History.changeRating(movie.getID(), newRating);
            movie.setUpdating(false);
            mRecyclerView.getAdapter().notifyItemChanged(position);
        }, 500);
    }

    private String[] getDialogOptions(int position) {
        if (mHistory.get(position).isUpdating()) {
            return new String[] {getString(R.string.show_similar_movies)};
        } else {
            return new String[] {getString(R.string.edit_rating),
                                 getString(R.string.show_similar_movies)};
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

    }*/

}
