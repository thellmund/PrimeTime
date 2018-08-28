package com.hellmund.primetime.ui.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hellmund.primetime.R;
import com.hellmund.primetime.model.SearchResult;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

class SearchAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<SearchResult> mItems;

    SearchAdapter(Context context, ArrayList<SearchResult> items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.list_item_search_results, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SearchResult item = mItems.get(position);

        Glide.with(mContext)
                .load(item.getPosterPath())
                .apply(new RequestOptions()
                    .placeholder(R.color.placeholder_grey)
                    .error(R.color.placeholder_grey))
             .into(holder.poster);

        final int resId = R.string.access_movie_poster_samples;
        holder.poster.setContentDescription(mContext.getString(resId) + item.getTitle());

        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());

        return convertView;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    ArrayList<SearchResult> getItems() {
        return mItems;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        @BindView(R.id.poster) ImageView poster;
        @BindView(R.id.title) TextView title;
        @BindView(R.id.description) TextView description;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
