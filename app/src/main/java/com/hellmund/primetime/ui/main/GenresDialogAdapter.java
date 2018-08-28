package com.hellmund.primetime.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hellmund.primetime.R;


class GenresDialogAdapter extends BaseAdapter {

    private static final int TYPE_BUTTON = 0;
    private static final int TYPE_TEXT = 1;
    private static final int TYPE_MAX_COUNT = 2;

    private Context mContext;
    private String[] mItems;

    GenresDialogAdapter(Context context, String[] items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int type = getItemViewType(position);
        ViewHolder holder;

        if (convertView == null) {
            int listItemResID = getListItem(type);
            convertView = LayoutInflater.from(mContext).inflate(listItemResID, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        setViewContent(position, holder);
        return convertView;
    }

    private int getListItem(int type) {
        if (type == TYPE_BUTTON) {
            return R.layout.list_item_genres_list;
        } else {
            return android.R.layout.simple_list_item_1;
        }
    }

    private void setViewContent(int position, ViewHolder holder) {
        if (position == 0) {
            holder.setIconView(R.drawable.ic_person_white_24dp);
        } else if (position == 1) {
            holder.setIconView(R.drawable.ic_search_white_24dp);
        }

        holder.setTextView(mItems[position]);
    }

    @Override
    public int getCount() {
        return mItems.length;
    }

    @Override
    public String getItem(int position) {
        return mItems[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position < 2 ? TYPE_BUTTON : TYPE_TEXT;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

    public static class ViewHolder {
        private ImageView iconView;
        private TextView textView;

        void setIconView(int resId) {
            this.iconView.setImageResource(resId);
        }

        void setTextView(String text) {
            this.textView.setText(text);
        }

        public ViewHolder(View view) {
            ImageView iconView = (ImageView) view.findViewById(R.id.icon);

            if (iconView == null) {
                this.textView = (TextView) view.findViewById(android.R.id.text1);
            } else {
                this.textView = (TextView) view.findViewById(R.id.text);
                this.iconView = iconView;
            }
        }
    }

}