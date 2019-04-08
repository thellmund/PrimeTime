package com.hellmund.primetime.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hellmund.primetime.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class DiscoverMoreFragment extends Fragment {

    private OnInteractionListener mCallback;
    private Unbinder mUnbinder;

    public static DiscoverMoreFragment newInstance() {
        return new DiscoverMoreFragment();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover_more, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        // mCallback = (OnInteractionListener) getContext();
        return view;
    }

    @OnClick(R.id.discover_more_btn)
    public void discoverMore() {
        mCallback.openCategories();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    public interface OnInteractionListener {
        void openCategories();
    }

}
