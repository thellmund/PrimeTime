@file:JvmName("Dialogs")
package com.hellmund.primetime.utils

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StringRes
import com.hellmund.primetime.R

fun Context.showLoading(messageResId: Int): ProgressDialog {
    return ProgressDialog(this).apply {
        setProgressStyle(ProgressDialog.STYLE_SPINNER)
        setMessage(context.getString(messageResId))
        show()
    }
}

@JvmOverloads
fun Context.showCancelable(
        @StringRes messageResId: Int,
        @StringRes positiveResId: Int,
        onPositive: ((dialog: DialogInterface) -> Unit)? = null,
        onNegative: ((dialog: DialogInterface) -> Unit)? = null
): Dialog {
    return AlertDialog.Builder(this)
            .setMessage(messageResId)
            .setNegativeButton(R.string.cancel) { dialog, _ -> onNegative?.invoke(dialog) }
            .setPositiveButton(positiveResId) { dialog, _ -> onPositive?.invoke(dialog) }
            .show()
}

fun Context.showItems(
        @StringRes titleResId: Int,
        items: Array<String>,
        onSelected: ((index: Int) -> Unit)? = null
): Dialog {
    return AlertDialog.Builder(this)
            .setTitle(titleResId)
            .setItems(items) { dialog, which -> onSelected?.invoke(which) }
            .setCancelable(true)
            .show()
}
