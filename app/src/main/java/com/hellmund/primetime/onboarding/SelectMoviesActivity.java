package com.hellmund.primetime.onboarding;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.GridView;

import com.hellmund.primetime.R;
import com.hellmund.primetime.model.Sample;
import com.hellmund.primetime.main.MainActivity;
import com.hellmund.primetime.utils.Constants;
import com.hellmund.primetime.utils.DeviceUtils;
import com.hellmund.primetime.utils.DownloadManager;
import com.hellmund.primetime.utils.UiUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SelectMoviesActivity extends AppCompatActivity
        implements SamplesAdapter.OnInteractionListener {

    private final static int MIN_COUNT = 4;

    private ArrayList<Sample> mSamples;
    private HashSet<Integer> mSelected;

    private SharedPreferences mSharedPrefs;

    @BindView(R.id.grid_view) GridView mGridView;
    @BindView(R.id.button) AppCompatButton mSaveButton;
    @BindView(R.id.error_button) AppCompatButton mErrorButton;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_movie);

        getWindow().setBackgroundDrawable(null);
        ButterKnife.bind(this);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        downloadMovies();
    }

    @OnClick(R.id.error_button)
    public void tryDownloadAgain(View view) {
        view.setVisibility(View.GONE);
        downloadMovies();
    }

    @Override
    public void onItemSelected(View view, int position) {
        toggleItemState(view, position);
        setSaveButtonState();
    }

    private void toggleItemState(View view, int position) {
        if (mSelected == null) {
            mSelected = new HashSet<>();
        }

        final int id = mSamples.get(position).getID();

        if (mSelected.contains(id)) {
            view.setAlpha(Constants.DISABLED);
            mSelected.remove(id);
        } else {
            view.setAlpha(Constants.ENABLED);
            mSelected.add(id);
        }
    }

    private void setSaveButtonState() {
        final boolean enabled = mSelected.size() >= MIN_COUNT;
        final float alpha = enabled ? Constants.ENABLED : Constants.DISABLED;
        mSaveButton.setClickable(enabled);
        mSaveButton.setAlpha(alpha);
    }

    private void displayError() {
        mGridView.setVisibility(View.GONE);
        mErrorButton.setVisibility(View.VISIBLE);
    }

    private void downloadMovies() {
        if (!DeviceUtils.isConnected(this)) {
            displayError();
            return;
        }

        displayProgressDialog();
        getSupportLoaderManager().initLoader(0, null,
                new LoaderManager.LoaderCallbacks<ArrayList<Sample>>() {
            @NonNull
            @Override
            public Loader<ArrayList<Sample>> onCreateLoader(int id, Bundle args) {
                return new DownloadMoviesLoader(getApplicationContext());
            }

            @Override
            public void onLoadFinished(@NonNull Loader<ArrayList<Sample>> loader, ArrayList<Sample> data) {
                mSamples = data;

                if (mSamples == null || mSamples.isEmpty()) {
                    displayError();
                    mGridView.setVisibility(View.GONE);
                    mErrorButton.setVisibility(View.VISIBLE);
                } else {
                    SamplesAdapter adapter =
                            new SamplesAdapter(SelectMoviesActivity.this, mSamples);
                    mGridView.setAdapter(adapter);

                    mGridView.setVisibility(View.VISIBLE);
                    mErrorButton.setVisibility(View.GONE);

                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onLoaderReset(@NonNull Loader<ArrayList<Sample>> loader) {}
        });
    }

    private void displayProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.downloading_samples));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @OnClick(R.id.button)
    public void saveMovies() {
        if (!mSaveButton.isEnabled()) {
            UiUtils.showToast(this, String.format(
                    Locale.getDefault(), getString(R.string.select_at_least), MIN_COUNT));
        }

        if (DeviceUtils.isConnected(this)) {
            saveSelection();
            markIntroDone();
            openRecommendations();
        } else {
            UiUtils.showToast(this, getString(R.string.not_connected));
        }
    }

    private void markIntroDone() {
        mSharedPrefs.edit().putBoolean("firstLaunchOfPrimeTime", false).apply();
    }

    private void openRecommendations() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveSelection() {
        ArrayList<Sample> results = new ArrayList<>();
        for (int i = 0; i < mSamples.size(); i++) {
            if (mSelected.contains(mSamples.get(i).getID())) {
                results.add(mSamples.get(i));
            }
        }
        // History.addSamples(results);
    }

    private static class DownloadMoviesLoader extends AsyncTaskLoader<ArrayList<Sample>> {

        private ArrayList<Sample> mResults;

        DownloadMoviesLoader(Context context) {
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
        public ArrayList<Sample> loadInBackground() {
            return DownloadManager.downloadSamples(getContext());
        }

        @Override
        public void deliverResult(ArrayList<Sample> results) {
            mResults = results;
            super.deliverResult(results);
        }

    }

}
