package com.hellmund.primetime.recommendations.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.hellmund.primetime.recommendations.R
import com.hellmund.primetime.recommendations.databinding.ViewBannerBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets

class MaterialBanner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var binding: ViewBannerBinding
    private var onDismissListener: () -> Unit = {}

    init {
        val view = View.inflate(context, R.layout.view_banner, this)
        binding = ViewBannerBinding.bind(view)
        binding.closeButton.setOnClickListener { dismiss() }

        doOnApplyWindowInsets { v, insets, initialState ->
            v.updateLayoutParams<MarginLayoutParams> {
                bottomMargin = initialState.margins.bottom + insets.systemWindowInsetBottom
            }
        }
    }

    fun setOnClickListener(listener: () -> Unit): MaterialBanner {
        binding.cardView.setOnClickListener { listener() }
        return this
    }

    fun setOnDismissListener(listener: () -> Unit): MaterialBanner {
        onDismissListener = listener
        return this
    }

    fun show() {
        isVisible = true
    }

    fun dismiss() {
        hideBanner {
            onDismissListener.invoke()
            isVisible = false
        }
    }

    private fun hideBanner(block: () -> Unit) {
        animate()
            .translationYBy(500f)
            .setDuration(300)
            .withEndAction(block)
            .start()
    }
}
