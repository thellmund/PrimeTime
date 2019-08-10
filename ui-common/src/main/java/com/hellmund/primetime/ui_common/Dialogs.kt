package com.hellmund.primetime.ui_common

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

@Suppress("DEPRECATION")
fun Context.showLoading(messageResId: Int): ProgressDialog {
    return ProgressDialog(this).apply {
        setProgressStyle(ProgressDialog.STYLE_SPINNER)
        setMessage(context.getString(messageResId))
        show()
    }
}

fun Context.showInfoDialog(messageResId: Int) {
    showInfoDialog(getString(messageResId))
}

fun Context.showInfoDialog(message: String) {
    AlertDialog.Builder(this)
        .setMessage(message)
        .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
        .show()
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
    title: String,
    items: Array<String>,
    onSelected: ((index: Int) -> Unit)? = null
): Dialog {
    return AlertDialog.Builder(this)
        .setTitle(title)
        .setItems(items) { _, which -> onSelected?.invoke(which) }
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

fun Context.showMultiSelectDialog(
    @StringRes titleResId: Int,
    items: Array<String>,
    checkedItems: BooleanArray,
    @StringRes positiveResId: Int,
    onConfirmed: (selected: List<Int>) -> Unit
) {
    AlertDialog.Builder(this)
        .setTitle(titleResId)
        .setMultiChoiceItems(items, checkedItems) { _, index, isSelected ->
            checkedItems[index] = isSelected
        }
        .setCancelable(true)
        .setNegativeButton(R.string.cancel, null)
        .setPositiveButton(positiveResId) { _, _ ->
            val selected = (0 until items.size).filterIndexed { i, _ -> checkedItems[i] }
            onConfirmed(selected)
        }
        .show()
}
