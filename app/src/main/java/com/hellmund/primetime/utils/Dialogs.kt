package com.hellmund.primetime.utils

import android.app.ProgressDialog
import android.content.Context

object Dialogs {

    @JvmStatic
    fun showLoading(context: Context, messageResId: Int): ProgressDialog {
        return ProgressDialog(context).apply {
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setMessage(context.getString(messageResId))
            show()
        }
    }

}
