package com.hellmund.primetime.ui.history;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hellmund.primetime.R;
import com.hellmund.primetime.model.HistoryMovie;
import com.hellmund.primetime.utils.UiUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private OnInteractionListener mParent;
    private Context mContext;
    private ArrayList<HistoryMovie> mItems;

    HistoryAdapter(Context context, OnInteractionListener listener, ArrayList<HistoryMovie> items) {
        mParent = listener;
        mContext = context;
        mItems = items;
    }

    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final HistoryAdapter.ViewHolder holder, int position) {
        HistoryMovie movie = mItems.get(position);
        holder.title.setText(movie.getTitle());

        if (mItems.get(position).isUpdating()) {
            holder.subtitle.setText(R.string.updating_rating);
            holder.subtitle.setTypeface(null, Typeface.ITALIC);
        } else {
            holder.subtitle.setText(movie.getSubhead(mContext));
            holder.subtitle.setTypeface(null, Typeface.NORMAL);
        }

        holder.button.setOnClickListener(v -> mParent.onOpenDialog(holder.getAdapterPosition()));

        holder.itemView.setTag(movie);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private boolean canRemove() {
        return getItemCount() > 4;
    }

    void removeItem(int position) {
        if (!canRemove()) {
            UiUtils.showToast(mContext, "Can’t remove more items.");
            this.notifyDataSetChanged();
            return;
        }

        HistoryMovie movie = mItems.get(position);

        mItems.remove(position);
        //History.remove(movie.getID());

        notifyItemRemoved(position);
        displayDeleteSnackbar(movie, position);
    }

    private void displayDeleteSnackbar(HistoryMovie movie, int position) {
        Snackbar.make(mParent.getContainer(), R.string.removed_from_history, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    //History.addHistoryMovie(movie);
                    mItems.add(position, movie);
                    notifyItemInserted(position);
                })
                .setActionTextColor(UiUtils.getSnackbarColor(mContext))
                .show();
    }

    public interface OnInteractionListener {
        void onOpenDialog(int position);
        View getContainer();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title) TextView title;
        @BindView(R.id.subtitle) TextView subtitle;
        @BindView(R.id.button) ImageView button;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
