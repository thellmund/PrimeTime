package com.hellmund.primetime.settings.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.hellmund.primetime.core.AddressableActivity
import com.hellmund.primetime.core.Preferences
import com.hellmund.primetime.core.coreComponent
import com.hellmund.primetime.core.createIntent
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.settings.R
import com.hellmund.primetime.settings.delegates.GenresDelegate
import com.hellmund.primetime.settings.delegates.GenresValidator
import com.hellmund.primetime.settings.delegates.ValidationResult.NotEnough
import com.hellmund.primetime.settings.delegates.ValidationResult.Overlap
import com.hellmund.primetime.settings.delegates.ValidationResult.Success
import com.hellmund.primetime.settings.di.DaggerSettingsComponent
import com.hellmund.primetime.settings.util.doOnPreferenceChange
import com.hellmund.primetime.settings.util.requirePreference
import com.hellmund.primetime.ui_common.dialogs.showInfoDialog
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
        DaggerSettingsComponent.builder()
            .core(coreComponent)
            .build()
            .inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initIncludedGenresPref()
        initExcludedGenresPref()
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
        return when (val result = genresValidator.validate(pref, newValue)) {
            is Success -> {
                genresDelegate.updateGenres(result.genres)
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

    companion object {
        fun newInstance() = SettingsFragment()
    }
}
