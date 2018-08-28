package com.hellmund.primetime.ui.introduction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.GridView;

import com.hellmund.primetime.R;
import com.hellmund.primetime.ui.SelectGenreActivity;
import com.hellmund.primetime.utils.DeviceUtils;
import com.hellmund.primetime.utils.DownloadManager;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class IntroductionActivity extends FragmentActivity {

    @BindView(R.id.grid_view) GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);

        getWindow().setBackgroundDrawable(null);
        ButterKnife.bind(this);

        if (DeviceUtils.isLandscapeMode(this)) {
            mGridView.setNumColumns(4);
        }

        downloadPopularMovies();
    }

    private void downloadPopularMovies() {
        getSupportLoaderManager().initLoader(0, null,
                new LoaderManager.LoaderCallbacks<ArrayList<String>>() {
            @NonNull
            @Override
            public Loader<ArrayList<String>> onCreateLoader(int id, Bundle args) {
                return new PopularMoviesLoader(IntroductionActivity.this);
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<String>> loader, ArrayList<String> data) {
                displayResults(data);
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<String>> loader) {}
        });
    }

    private void displayResults(ArrayList<String> results) {
        PostersAdapter adapter = new PostersAdapter(this, results);
        mGridView.setAdapter(adapter);
        mGridView.setVisibility(View.VISIBLE);

        mGridView.setOnTouchListener((v, event) -> event.getAction() == MotionEvent.ACTION_MOVE);

        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(700);
        mGridView.setAnimation(fadeIn);
    }

    @OnClick(R.id.introduction_btn)
    public void openGenresSelection() {
        Intent intent = new Intent(this, SelectGenreActivity.class);
        startActivity(intent);
    }

    private static class PopularMoviesLoader extends AsyncTaskLoader<ArrayList<String>> {

        private ArrayList<String> mResults;

        PopularMoviesLoader(Context context) {
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
        public ArrayList<String> loadInBackground() {
            return DownloadManager.downloadMostPopularMoviePosters();
        }

        @Override
        public void deliverResult(ArrayList<String> results) {
            mResults = results;
            super.deliverResult(results);
        }

    }

}