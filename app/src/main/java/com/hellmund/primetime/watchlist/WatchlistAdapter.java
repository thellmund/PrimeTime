package com.hellmund.primetime.watchlist;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.hellmund.primetime.database.WatchlistMovie;

import java.util.ArrayList;
import java.util.List;

public class WatchlistAdapter extends FragmentStatePagerAdapter {

    private List<WatchlistMovie> movies;
    private WatchlistMovieFragment.OnInteractionListener listener;

    public WatchlistAdapter(FragmentManager fragmentManager,
                            WatchlistMovieFragment.OnInteractionListener listener) {
        super(fragmentManager);
        this.movies = new ArrayList<>();
        this.listener = listener;
    }

    public void update(List<WatchlistMovie> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return WatchlistMovieFragment.newInstance(movies.get(position), position, listener);
    }

    @Override
    public int getCount() {
        return movies.size();
    }

    /*@Override
    public float getPageWidth(int position) {
        return DeviceUtils.isLandscapeMode(mContext) ? 0.5f : 1.0f;
    }*/

}
