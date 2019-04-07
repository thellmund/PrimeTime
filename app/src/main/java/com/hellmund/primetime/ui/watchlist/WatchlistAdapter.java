package com.hellmund.primetime.ui.watchlist;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.hellmund.primetime.utils.DeviceUtils;

public class WatchlistAdapter extends FragmentStatePagerAdapter {

    private Context mContext;
    private int mCount;

    public WatchlistAdapter(FragmentManager fragmentManager, Context context, int count) {
        super(fragmentManager);
        this.mContext = context;
        this.mCount = count;
    }

    @Override
    public Fragment getItem(int position) {
        return WatchlistMovieFragment.newInstance(mContext, position);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public float getPageWidth(int position) {
        return DeviceUtils.isLandscapeMode(mContext) ? 0.5f : 1.0f;
    }

}
