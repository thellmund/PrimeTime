package com.hellmund.primetime.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.transition.TransitionManager
import com.hellmund.primetime.R
import kotlinx.android.synthetic.main.view_banner.view.*

class MaterialBanner @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.view_banner, this)
        negativeButton.setOnClickListener { dismiss() }
    }

    fun setOnClickListener(listener: () -> Unit) {
        positiveButton.setOnClickListener {
            listener()
        }
    }

    fun show() {
        TransitionManager.beginDelayedTransition(parent as ViewGroup)
        visibility = View.VISIBLE
    }

    fun dismiss() {
        TransitionManager.beginDelayedTransition(parent as ViewGroup)
        visibility = View.GONE
    }

}
