package com.hellmund.primetime.ui.selectmovies;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hellmund.primetime.R;
import com.hellmund.primetime.model.Sample;
import com.hellmund.primetime.utils.Constants;
import com.hellmund.primetime.utils.DownloadUtils;
import com.hellmund.primetime.utils.UiUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SamplesAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Sample> mSamples;
    private OnInteractionListener mCallback;

    SamplesAdapter(Activity activity, ArrayList<Sample> samples) {
        this.mContext = activity.getApplicationContext();
        this.mSamples = samples;
        this.mCallback = (OnInteractionListener) activity;
    }

    public interface OnInteractionListener {
        void onItemSelected(View view, int position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            final int resId = R.layout.list_item_samples_list;
            convertView = LayoutInflater.from(mContext).inflate(resId, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mSamples.get(position).isSelected()) {
            holder.container.setAlpha(Constants.ENABLED);
        } else {
            holder.container.setAlpha(Constants.DISABLED);
        }

        holder.container.setOnClickListener(v -> {
            mCallback.onItemSelected(v, position);
            mSamples.get(position).toggleSelection();
        });

        holder.container.setOnLongClickListener(v -> {
            UiUtils.showToast(mContext, mSamples.get(position).getTitle());
            return true;
        });

        final String url = DownloadUtils.getLowResPosterURL(mSamples.get(position).getPoster());
        Glide.with(mContext)
                .load(url)
                .apply(new RequestOptions().centerCrop())
                .into(holder.poster);

        final int resId = R.string.access_movie_poster_samples;
        holder.poster.setContentDescription(
                mContext.getString(resId) + mSamples.get(position).getTitle());

        return convertView;
    }

    @Override
    public int getCount() {
        return mSamples.size();
    }

    @Override
    public Sample getItem(int position) {
        return mSamples.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        @BindView(R.id.container) FrameLayout container;
        @BindView(R.id.poster) ImageView poster;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

    }

}
