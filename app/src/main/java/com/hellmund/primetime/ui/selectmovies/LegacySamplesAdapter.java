package com.hellmund.primetime.ui.selectmovies;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hellmund.primetime.R;
import com.hellmund.primetime.data.model.Sample;
import com.hellmund.primetime.utils.Constants;
import com.hellmund.primetime.utils.ImageLoader;
import com.hellmund.primetime.utils.UiUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

@Deprecated
public class LegacySamplesAdapter extends BaseAdapter {

    private Context mContext;
    private List<Sample> mSamples;
    private OnInteractionListener mCallback;

    LegacySamplesAdapter(Activity activity, List<Sample> samples) {
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

        Sample sample = mSamples.get(position);

        if (sample.getSelected()) {
            holder.container.setAlpha(Constants.ENABLED);
        } else {
            holder.container.setAlpha(Constants.DISABLED);
        }

        holder.container.setOnClickListener(v -> {
            // mCallback.onItemSelected(v, position);
            // sample.toggleSelected();
        });

        holder.container.setOnLongClickListener(v -> {
            UiUtils.showToast(mContext, sample.getTitle());
            return true;
        });

        final String url = sample.getFullPosterUrl();
        ImageLoader.with(mContext).load(url, holder.poster);

        final int resId = R.string.access_movie_poster_samples;
        holder.poster.setContentDescription(mContext.getString(resId) + sample.getTitle());

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

    static class ViewHolder {
        @BindView(R.id.container) FrameLayout container;
        @BindView(R.id.posterImageView) ImageView poster;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

    }

}
