package com.hellmund.primetime.ui.watchlist;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;

import com.hellmund.primetime.R;
import com.hellmund.primetime.model.WatchlistMovie;
import com.hellmund.primetime.utils.Constants;
import com.hellmund.primetime.utils.UiUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnPageChange;
import me.relex.circleindicator.CircleIndicator;

public class WatchlistActivity extends AppCompatActivity
        implements WatchlistFragment.OnInteractionListener {

    @BindView(R.id.content) LinearLayout mContent;
    @BindView(R.id.view_pager) ViewPager mViewPager;
    @BindView(R.id.indicator2) CircleIndicator mIndicator;
    @BindView(R.id.placeholder) LinearLayout mPlaceholder;

    private ArrayList<WatchlistMovie> mMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist);

        ButterKnife.bind(this);
        initToolbar();

        mMovies = new ArrayList<>(); // Watchlist.get();
        toggleListAndPlaceholder();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @OnClick(R.id.placeholder_btn)
    public void openRecommendations() {
        onBackPressed();
    }

    private void toggleListAndPlaceholder() {
        WatchlistAdapter adapter =
                new WatchlistAdapter(getSupportFragmentManager(), this, mMovies.size());
        mViewPager.setAdapter(adapter);
        mIndicator.setViewPager(mViewPager);

        if (mMovies.isEmpty()) {
            AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
            animation.setDuration(300);
            mContent.setAnimation(animation);
            mContent.animate();
            mPlaceholder.setVisibility(View.VISIBLE);
        } else {
            AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
            animation.setDuration(300);
            mContent.setAnimation(animation);
            mContent.animate();
            mPlaceholder.setVisibility(View.GONE);
        }
    }

    @OnPageChange(R.id.view_pager)
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            final int position = mViewPager.getCurrentItem();
            final int prev = position - 1;
            final int next = position + 1;

            if (prev >= 0 && mMovies.get(prev).isDeleted()) {
                mMovies.remove(prev);
                mViewPager.setAdapter(new WatchlistAdapter(
                        getSupportFragmentManager(), WatchlistActivity.this, mMovies.size()));
                mViewPager.setCurrentItem(prev);
                mIndicator.setViewPager(mViewPager);
            }

            if (next < mMovies.size() && mMovies.get(next).isDeleted()) {
                mMovies.remove(next);
                mViewPager.setAdapter(new WatchlistAdapter(
                        getSupportFragmentManager(), WatchlistActivity.this, mMovies.size()));
                mViewPager.setCurrentItem(next);
                mIndicator.setViewPager(mViewPager);
            }
        }
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
    public void onWatchedIt(final int position) {
        final WatchlistMovie movie = mMovies.get(position);
        final String title = movie.getTitle();
        final String header = String.format(getString(R.string.rate_movie), title);
        final String[] options = {getString(R.string.like), getString(R.string.dislike)};

        new AlertDialog.Builder(this)
                .setTitle(header)
                .setItems(options, (dialog, which) -> {
                    final int rating = (which == 0) ? Constants.LIKE : Constants.DISLIKE;
                    rateMovie(movie, position, rating);
                }).show();
    }

    private void rateMovie(final WatchlistMovie movie, final int position, int rating) {
        // History.add(movie, rating);
        // Watchlist.remove(movie.getID());

        mMovies.get(position).delete();
        final int newPosition = getPositionOfNextItem(position);
        scrollToNextPosition(newPosition);

        String message;

        if (rating == Constants.LIKE) {
            message = getString(R.string.will_more_like_this);
        } else {
            message = getString(R.string.will_less_like_this);
        }

        Snackbar.make(mViewPager, message, Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setAction(R.string.undo, v -> {
                    // History.remove(movie.getID());
                    // Watchlist.restore(movie);
                    movie.undelete();
                    restoreInViewPager(movie, position);
                })
                .show();
    }

    @Override
    public void onRemove(int position) {
        WatchlistMovie movie = mMovies.get(position);
        final int newPosition = getPositionOfNextItem(position);
        movie.delete();

        // Watchlist.remove(movie.getID());
        scrollToNextPosition(newPosition);
        displayRemoveSnackbar(movie, position);
    }

    private void scrollToNextPosition(int newPosition) {
        if (newPosition == -1) {
            mMovies.remove(0);
            toggleListAndPlaceholder();
        } else {
            mViewPager.setCurrentItem(newPosition);
        }
    }

    private void displayRemoveSnackbar(WatchlistMovie movie, int position) {
        Snackbar.make(mViewPager, R.string.watchlist_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    // Watchlist.restore(movie);
                    movie.undelete();
                    restoreInViewPager(movie, position);
                })
                .setActionTextColor(UiUtils.getSnackbarColor(this))
                .show();
    }

    private int getPositionOfNextItem(int position) {
        final int size = mMovies.size();

        if (size == 1) {
            return -1;
        }

        if (position == size - 1) {
            return position - 1;
        } else {
            return position + 1;
        }
    }

    private void restoreInViewPager(WatchlistMovie movie, int position) {
        mMovies.add(position, movie);
        toggleListAndPlaceholder();
        mViewPager.setCurrentItem(position);
    }

    @Override
    public WatchlistMovie onGetMovie(int position) {
        return mMovies.get(position);
    }

}
