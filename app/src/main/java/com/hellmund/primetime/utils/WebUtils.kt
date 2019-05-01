@file:JvmName("WebUtils")
package com.hellmund.primetime.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.JELLY_BEAN_MR2
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.hellmund.primetime.R

fun Context.openUrl(url: String) {
    if (SDK_INT >= JELLY_BEAN_MR2) {
        val color = colorFromResource(R.color.colorPrimary)
        CustomTabsIntent.Builder()
                .setToolbarColor(color)
                .build()
                .launchUrl(this, url.toUri())
    } else {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}

private fun String.toUri(): Uri = Uri.parse(this)

@ColorInt
private fun Context.colorFromResource(@ColorRes resId: Int): Int = ContextCompat.getColor(this, resId)
