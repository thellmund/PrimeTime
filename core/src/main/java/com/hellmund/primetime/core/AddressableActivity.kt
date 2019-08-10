package com.hellmund.primetime.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

enum class AddressableActivity(val className: String) {
    About("com.hellmund.primetime.about.AboutActivity"),
    History("com.hellmund.primetime.history.ui.HistoryActivity"),
    Main("com.hellmund.primetime.ui.MainActivity"),
    Onboarding("com.hellmund.primetime.ui.onboarding.OnboardingActivity")
}

enum class AddressableFragment(val className: String) {
    Home("com.hellmund.primetime.ui.suggestions.HomeFragment"),
    MovieDetails("com.hellmund.primetime.moviedetails.ui.MovieDetailsFragment")
}

object FragmentArgs {
    const val KEY_MOVIE = "KEY_MOVIE"
}

class FragmentFactory @Inject constructor(private val context: Context) {

    fun movieDetails(bundle: Bundle): BottomSheetDialogFragment {
        val fragment = context.createFragment(AddressableFragment.MovieDetails)
        fragment.arguments = bundle
        return fragment as BottomSheetDialogFragment
    }

}

fun Context.createIntent(
    addressableActivity: AddressableActivity
) = Intent(this, Class.forName(addressableActivity.className))

fun Context.createFragment(
    addressableFragment: AddressableFragment
): Fragment = Fragment.instantiate(this, addressableFragment.className)
