package com.hellmund.primetime.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import javax.inject.Inject

enum class AddressableActivity(val className: String) {
    About("com.hellmund.primetime.about.AboutActivity"),
    History("com.hellmund.primetime.history.ui.HistoryActivity"),
    Main("com.hellmund.primetime.ui.MainActivity"),
    Onboarding("com.hellmund.primetime.onboarding.OnboardingActivity"),
    Settings("com.hellmund.primetime.settings.ui.SettingsActivity")
}

enum class AddressableFragment(val className: String) {
    Home("com.hellmund.primetime.recommendations.ui.HomeFragment"),
    MovieDetails("com.hellmund.primetime.moviedetails.ui.MovieDetailsFragment")
}

object FragmentArgs {
    const val KEY_MOVIE = "KEY_MOVIE"
    const val KEY_RECOMMENDATIONS_TYPE = "KEY_RECOMMENDATIONS_TYPE"
}

class FragmentFactory @Inject constructor(private val context: Context) {

    fun movieDetails(bundle: Bundle): Fragment {
        val fragment = context.createFragment(AddressableFragment.MovieDetails)
        fragment.arguments = bundle
        return fragment
    }

    fun category(bundle: Bundle): Fragment {
        return context.createFragment(AddressableFragment.Home).apply { arguments = bundle }
    }

}

fun Context.createIntent(
    addressableActivity: AddressableActivity
) = Intent(this, Class.forName(addressableActivity.className))

fun Context.createFragment(
    addressableFragment: AddressableFragment
): Fragment = Fragment.instantiate(this, addressableFragment.className)
