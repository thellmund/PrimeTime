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
            dismiss()
        }
    }

    // private val bannerView = View.inflate(context, R.layout.view_banner, null)

    /*fun setIcon(resId: Int): MaterialBanner {
        bannerView.iconImageView.setImageResource(resId)
        return this
    }

    fun setTitle(title: String): MaterialBanner {
        bannerView.titleTextView.apply {
            text = title
            visibility = View.VISIBLE
        }
        return this
    }

    fun setMessage(resId: Int) = setMessage(context.getString(resId))

    fun setMessage(message: String): MaterialBanner {
        bannerView.messageTextView.text = message
        return this
    }

    fun setNegativeButton(resId: Int, callback: (MaterialBanner) -> Unit): MaterialBanner {
        bannerView.negativeButton.apply {
            text = context.getString(resId)
            setOnClickListener {
                callback(this@MaterialBanner)
                dismiss()
            }
        }
        return this
    }

    fun setPositiveButton(resId: Int, callback: (MaterialBanner) -> Unit): MaterialBanner {
        bannerView.positiveButton.apply {
            text = context.getString(resId)
            setOnClickListener {
                callback(this@MaterialBanner)
                dismiss()
            }
        }
        return this
    }*/

    fun show() {
        TransitionManager.beginDelayedTransition(parent as ViewGroup)
        visibility = View.VISIBLE
        // (parent as ViewGroup).addView(bannerView, 0)
    }

    private fun dismiss() {
        TransitionManager.beginDelayedTransition(parent as ViewGroup)
        visibility = View.GONE
        // (parent as ViewGroup).removeView(bannerView)
    }

}
