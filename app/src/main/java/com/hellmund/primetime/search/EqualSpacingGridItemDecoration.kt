package com.hellmund.primetime.search

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class EqualSpacingGridItemDecoration(
        private val spacing: Int,
        private val spanCount: Int = 2
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        outRect.left = spacing - column * spacing / spanCount
        outRect.right = (column + 1) * spacing / spanCount

        if (position < spanCount) {
            outRect.top = spacing
        }
        outRect.bottom = spacing
    }

}