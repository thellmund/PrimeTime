package com.hellmund.primetime.recommendations.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.transition.TransitionManager
import com.hellmund.primetime.recommendations.R
import com.hellmund.primetime.ui_common.util.updatePadding
import kotlinx.android.synthetic.main.view_banner.view.negativeButton
import kotlinx.android.synthetic.main.view_banner.view.positiveButton

class MaterialBanner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val container: ViewGroup?
        get() = parent as? ViewGroup

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
        container?.updatePadding(top = height)
    }

    fun dismiss() {
        TransitionManager.beginDelayedTransition(parent as ViewGroup)
        visibility = View.GONE
        container?.updatePadding(top = 0)
    }
}
