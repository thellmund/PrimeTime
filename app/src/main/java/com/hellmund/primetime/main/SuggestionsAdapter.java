package com.hellmund.primetime.main;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.hellmund.primetime.utils.Constants;
import com.hellmund.primetime.utils.DeviceUtils;

class SuggestionsAdapter extends FragmentStatePagerAdapter {

    private int mViewState;
    private int mCount;
    private Context mContext;

    private SuggestionFragment.OnInteractionListener listener;
    private SuggestionErrorFragment.OnInteractionListener errorListener;

    SuggestionsAdapter(FragmentManager fragmentMgr, Context context, int viewState,
                       int count, SuggestionFragment.OnInteractionListener listener,
                       SuggestionErrorFragment.OnInteractionListener errorListener) {
        super(fragmentMgr);
        this.listener = listener;
        this.errorListener = errorListener;
        this.mContext = context;
        this.mViewState = viewState;
        this.mCount = count;
    }

    @Override
    public Fragment getItem(int position) {
        if (mViewState == Constants.IDEAL_STATE && position == mCount - 1) {
            return DiscoverMoreFragment.newInstance();
        } else if (mViewState == Constants.IDEAL_STATE) {
            return SuggestionFragment.newInstance(position, listener);
        } else {
            return SuggestionErrorFragment.newInstance(mViewState, errorListener);
        }
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