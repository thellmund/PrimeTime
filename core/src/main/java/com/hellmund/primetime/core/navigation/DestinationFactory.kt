package com.hellmund.primetime.core.navigation

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.hellmund.primetime.core.navigation.DestinationsArgs.KEY_MOVIE
import com.hellmund.primetime.core.navigation.DestinationsArgs.KEY_RECOMMENDATIONS_TYPE
import javax.inject.Inject

enum class AddressableActivity(val className: String) {
    About("com.hellmund.primetime.about.AboutActivity"),
    History("com.hellmund.primetime.history.ui.HistoryActivity"),
    Main("com.hellmund.primetime.ui.MainActivity"),
    MovieDetails("com.hellmund.primetime.moviedetails.ui.MovieDetailsActivity"),
    Onboarding("com.hellmund.primetime.onboarding.OnboardingActivity"),
    Settings("com.hellmund.primetime.settings.ui.SettingsActivity")
}

enum class AddressableFragment(val className: String) {
    Home("com.hellmund.primetime.recommendations.ui.HomeFragment"),
}

object DestinationsArgs {
    const val KEY_MOVIE = "KEY_MOVIE"
    const val KEY_RECOMMENDATIONS_TYPE = "KEY_RECOMMENDATIONS_TYPE"
}

class DestinationFactory @Inject constructor(private val context: Context) {

    fun movieDetails(
        movie: Parcelable
    ): Intent {
        val args = bundleOf(KEY_MOVIE to movie)
        return context.createIntent(AddressableActivity.MovieDetails).putExtras(args)
    }

    fun category(
        category: Parcelable
    ): Fragment {
        val args = bundleOf(KEY_RECOMMENDATIONS_TYPE to category)
        return context.createFragment(AddressableFragment.Home).apply { arguments = args }
    }
}

fun Context.createIntent(
    addressableActivity: AddressableActivity
) = Intent(this, Class.forName(addressableActivity.className))

fun Context.createFragment(
    addressableFragment: AddressableFragment
): Fragment = Fragment.instantiate(this, addressableFragment.className)
