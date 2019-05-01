package com.hellmund.primetime.ui.history

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.support.v7.widget.helper.ItemTouchHelper.*
import android.view.View
import java.lang.Math.abs

internal class HistoryItemTouchHelper(
        context: Context,
        private val adapter: HistoryAdapter
) : ItemTouchHelper.Callback() {

    private val background = ContextCompat.getDrawable(context, android.R.color.transparent)

    override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
    ): Int = makeFlag(ACTION_STATE_SWIPE, END) or makeFlag(ACTION_STATE_SWIPE, START)

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
        val position = holder.adapterPosition
        adapter.removeItem(position)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView,
                             holder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                             actionState: Int, isCurrentlyActive: Boolean) {
        if (holder.adapterPosition == -1) {
            return
        }

        val width = holder.itemView.width.toFloat()
        val percentage = 1 - abs(dX) / width
        holder.itemView.alpha = percentage

        background?.bounds = holder.itemView.rect.add(right = dX.toInt())
        background?.draw(c)

        super.onChildDraw(c, recyclerView, holder, dX, dY, actionState, isCurrentlyActive)
    }

    private val View.rect: Rect
        get() = Rect(left, top, right, bottom)

    private fun Rect.add(
            left: Int = 0,
            top: Int = 0,
            right: Int = 0,
            bottom: Int = 0
    ) = Rect(this.left + left, this.top + top, this.right + right, this.bottom + bottom)

}
