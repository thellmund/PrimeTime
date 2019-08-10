package com.hellmund.primetime.ui_common

import android.content.Context
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat

fun Context.openUrl(url: String) {
    val color = colorFromResource(R.color.grey_900)
    CustomTabsIntent.Builder()
        .setToolbarColor(color)
        .build()
        .launchUrl(this, url.toUri())
}

private fun String.toUri(): Uri = Uri.parse(this)

@ColorInt
private fun Context.colorFromResource(@ColorRes resId: Int): Int = ContextCompat.getColor(this, resId)
