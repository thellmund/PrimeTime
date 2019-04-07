package com.hellmund.primetime.history;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

class HistoryItemTouchHelper extends ItemTouchHelper.Callback {

    private HistoryAdapter mAdapter;
    private Drawable mBackground;

    HistoryItemTouchHelper(Context context, HistoryAdapter adapter) {
        mAdapter = adapter;
        mBackground = ContextCompat.getDrawable(context, android.R.color.transparent);
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.END)
                | makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.START);
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView,
                          RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder holder, int direction) {
        final int position = holder.getAdapterPosition();
        mAdapter.removeItem(position);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                            RecyclerView.ViewHolder holder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        if (holder.getAdapterPosition() == -1) {
            return;
        }

        final float width = holder.itemView.getWidth();
        final float percentage = 1 - (Math.abs(dX) / width);
        holder.itemView.setAlpha(percentage);

        mBackground.setBounds(getBackgroundRect((int) dX, holder.itemView));
        mBackground.draw(c);

        super.onChildDraw(c, recyclerView, holder, dX, dY, actionState, isCurrentlyActive);
    }

    private Rect getBackgroundRect(int dX, View view) {
        final int left = view.getLeft();
        final int top = view.getTop();
        final int right = view.getLeft() + dX;
        final int bottom = view.getBottom();
        return new Rect(left, top, right, bottom);
    }

}
