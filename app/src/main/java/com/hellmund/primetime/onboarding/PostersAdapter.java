package com.hellmund.primetime.onboarding;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hellmund.primetime.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class PostersAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mUrls;

    PostersAdapter(Context context, List<String> urls) {
        this.mContext = context;
        this.mUrls = urls;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.list_item_introduction_bg, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Glide.with(mContext)
                .load(mUrls.get(position))
                .apply(new RequestOptions()
                        .placeholder(R.drawable.poster_placeholder)
                        .centerCrop())
                .into(holder.posterView);
        return convertView;
    }

    @Override
    public int getCount() {
        return mUrls.size();
    }

    @Override
    public String getItem(int position) {
        return mUrls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        @BindView(R.id.posterImageView) ImageView posterView;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
