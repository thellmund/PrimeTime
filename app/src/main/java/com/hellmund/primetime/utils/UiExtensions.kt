@file:JvmName("UiUtils")
package com.hellmund.primetime.utils

import android.content.Context
import android.widget.Toast

@JvmOverloads
fun Context.showToast(resId: Int, length: Int = Toast.LENGTH_SHORT) {
    showToast(getString(resId), length)
}

@JvmOverloads
fun Context.showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}
