package com.hellmund.primetime.main;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.hellmund.primetime.model2.ApiMovie;
import com.hellmund.primetime.utils.Constants;
import com.hellmund.primetime.utils.DeviceUtils;

import java.util.List;

class SuggestionsAdapter extends FragmentStatePagerAdapter {

    private int mViewState;
    private Context mContext;
    private SuggestionFragment.ViewPagerHost viewPagerHost;

    private List<ApiMovie> movies;

    SuggestionsAdapter(FragmentManager fragmentMgr, Context context, int viewState,
                       SuggestionFragment.ViewPagerHost viewPagerHost, List<ApiMovie> movies) {
        super(fragmentMgr);
        this.mContext = context;
        this.viewPagerHost = viewPagerHost;
        this.mViewState = viewState;
        this.movies = movies;
    }

    @Override
    public Fragment getItem(int position) {
        if (mViewState == Constants.IDEAL_STATE && position == movies.size() - 1) {
            return DiscoverMoreFragment.newInstance();
        } else if (mViewState == Constants.IDEAL_STATE) {
            return SuggestionFragment.newInstance(viewPagerHost, movies.get(position));
        } else {
            return SuggestionErrorFragment.newInstance(mViewState);
        }
    }

    @Override
    public int getCount() {
        return movies.size();
    }

    @Override
    public float getPageWidth(int position) {
        return DeviceUtils.isLandscapeMode(mContext) ? 0.5f : 1.0f;
    }

}