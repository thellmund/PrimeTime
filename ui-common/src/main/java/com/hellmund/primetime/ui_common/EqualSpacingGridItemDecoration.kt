package com.hellmund.primetime.ui_common

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class EqualSpacingGridItemDecoration @JvmOverloads constructor(
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
