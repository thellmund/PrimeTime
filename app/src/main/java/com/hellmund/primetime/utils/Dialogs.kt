@file:JvmName("Dialogs")
package com.hellmund.primetime.utils

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.hellmund.primetime.R

fun Context.showLoading(messageResId: Int): ProgressDialog {
    return ProgressDialog(this).apply {
        setProgressStyle(ProgressDialog.STYLE_SPINNER)
        setMessage(context.getString(messageResId))
        show()
    }
}

@JvmOverloads
fun Context.showCancelableDialog(
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

fun Context.showItemsDialog(
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

fun Context.showSingleSelectDialog(
        @StringRes titleResId: Int,
        choices: Array<String>,
        checked: Int = 0,
        @StringRes positiveResId: Int,
        onSelected: (index: Int) -> Unit
): Dialog {
    return AlertDialog.Builder(this)
            .setTitle(titleResId)
            .setSingleChoiceItems(choices, checked, null)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(positiveResId) { dialog, _ ->
                val dialogListView = (dialog as AlertDialog).listView
                val selected = dialogListView.checkedItemPosition
                onSelected(selected)
            }
            .setCancelable(true)
            .show()
}
