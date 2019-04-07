package com.hellmund.primetime.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hellmund.primetime.R;
import com.hellmund.primetime.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SuggestionErrorFragment extends Fragment {

    private OnInteractionListener mCallback;
    private int mViewState;
    private Unbinder mUnbinder;

    @BindView(R.id.error_title) TextView mTitle;
    @BindView(R.id.error_text) TextView mText;
    @BindView(R.id.error_btn) AppCompatButton mTryAgainButton;

    public static SuggestionErrorFragment newInstance(int viewState) {
        SuggestionErrorFragment fragment = new SuggestionErrorFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.VIEW_STATE, viewState);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewState = getArguments().getInt(Constants.VIEW_STATE);
        mCallback = (OnInteractionListener) getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.fragment_movie_suggestion_error, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        if (mViewState == Constants.EMPTY_STATE) {
            mTitle.setText(R.string.no_similar_movies_header);
            mText.setText(R.string.no_similar_movies_text);
            mTryAgainButton.setText(R.string.try_it);
        }

        return view;
    }

    @OnClick(R.id.error_btn)
    public void tryDownloadAgain() {
        if (mViewState == Constants.EMPTY_STATE) {
            mCallback.openCategories();
        } else {
            mCallback.tryDownloadAgain();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    public interface OnInteractionListener {
        void openCategories();
        void tryDownloadAgain();
    }

}
