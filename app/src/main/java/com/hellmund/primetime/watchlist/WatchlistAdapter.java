package com.hellmund.primetime.watchlist;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.hellmund.primetime.database.WatchlistMovie;

import java.util.List;

public class WatchlistAdapter extends FragmentStatePagerAdapter {

    private List<WatchlistMovie> movies;

    public WatchlistAdapter(FragmentManager fragmentManager, List<WatchlistMovie> movies) {
        super(fragmentManager);
        this.movies = movies;
    }

    @Override
    public Fragment getItem(int position) {
        return WatchlistMovieFragment.newInstance(movies.get(position));
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
