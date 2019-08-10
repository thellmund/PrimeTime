package com.hellmund.primetime.core

import android.content.Context
import android.content.Intent

enum class AddressableActivity(val className: String) {
    Main("com.hellmund.primetime.ui.MainActivity"),
    Onboarding("com.hellmund.primetime.ui.onboarding.OnboardingActivity")
}

fun Context.createIntent(
    addressableActivity: AddressableActivity
) = Intent(this, Class.forName(addressableActivity.className))
