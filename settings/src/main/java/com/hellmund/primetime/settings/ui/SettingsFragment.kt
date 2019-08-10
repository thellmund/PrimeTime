package com.hellmund.primetime.settings.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.hellmund.primetime.core.AddressableActivity
import com.hellmund.primetime.core.createIntent
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.settings.R
import com.hellmund.primetime.settings.delegates.GenresDelegate
import com.hellmund.primetime.settings.delegates.GenresValidator
import com.hellmund.primetime.settings.delegates.ValidationResult.NotEnough
import com.hellmund.primetime.settings.delegates.ValidationResult.Overlap
import com.hellmund.primetime.settings.delegates.ValidationResult.Success
import com.hellmund.primetime.settings.util.Preferences
import com.hellmund.primetime.settings.util.doOnPreferenceChange
import com.hellmund.primetime.settings.util.requirePreference
import com.hellmund.primetime.ui_common.openUrl
import com.hellmund.primetime.ui_common.showInfoDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var genresDelegate: GenresDelegate

    @Inject
    lateinit var genresValidator: GenresValidator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as Injector).injectSettingsFragment(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        initIncludedGenresPref()
        initExcludedGenresPref()
        initRateAppPref()
        initAboutAppPref()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    private fun initIncludedGenresPref() {
        val preference = requirePreference<MultiSelectListPreference>(Preferences.KEY_INCLUDED)
        lifecycleScope.launch(Dispatchers.IO) {
            genresDelegate.init(preference)
        }
        preference.doOnPreferenceChange(lifecycleScope, this::saveGenresSelection)
    }

    private fun initExcludedGenresPref() {
        val preference = requirePreference<MultiSelectListPreference>(Preferences.KEY_EXCLUDED)
        lifecycleScope.launch(Dispatchers.IO) {
            genresDelegate.init(preference)
        }
        preference.doOnPreferenceChange(lifecycleScope, this::saveGenresSelection)
    }

    private suspend fun saveGenresSelection(pref: Preference, newValue: Any): Boolean {
        val result = genresValidator.validate(pref, newValue)
        return when (result) {
            is Success -> {
                genresDelegate.updateGenresSummary(pref, result.genres)
                true
            }
            is NotEnough -> {
                displayNotEnoughCheckedAlert()
                false
            }
            is Overlap -> {
                displaySharedGenresAlert(result.genres)
                false
            }
        }
    }

    private fun displayNotEnoughCheckedAlert() {
        requireContext().showInfoDialog(R.string.need_more_genres)
    }

    private fun displaySharedGenresAlert(genres: List<Genre>) {
        val sharedGenres = genres.joinToString("\n") { "• ${it.name}" }
        val error = getString(R.string.error_already_in_genres, sharedGenres)
        requireContext().showInfoDialog(error)
    }

    private fun initRateAppPref() {
        val ratePrimeTime = requirePreference<Preference>(Preferences.KEY_PLAY_STORE)

        ratePrimeTime.setOnPreferenceClickListener {
            openPlayStore()
            true
        }
    }

    private fun openPlayStore() {
        val packageName = requireActivity().packageName

        try {
            requireContext().openUrl("market://details?id=$packageName")
        } catch (e: ActivityNotFoundException) {
            requireContext().openUrl("https://play.google.com/store/apps/details?id=$packageName")
        }
    }

    private fun initAboutAppPref() {
        val about = requirePreference<Preference>(Preferences.KEY_ABOUT)

        val version = getVersionName()
        about.summary = version?.let { "Version $it" }

        about.setOnPreferenceClickListener {
            val intent = requireContext().createIntent(AddressableActivity.About)
            startActivity(intent)
            true
        }
    }

    private fun getVersionName(): String? {
        return try {
            requireActivity()
                .packageManager
                .getPackageInfo(requireActivity().packageName, 0)
                .versionName
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    interface Injector {
        fun injectSettingsFragment(settingsFragment: SettingsFragment)
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }

}
