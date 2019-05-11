package com.hellmund.primetime.ui.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import com.hellmund.primetime.R
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.ui.about.AboutActivity
import com.hellmund.primetime.ui.selectgenres.GenresRepository
import com.hellmund.primetime.ui.selectstreamingservices.StreamingServicesStore
import com.hellmund.primetime.ui.settings.delegates.GenresDelegate
import com.hellmund.primetime.ui.settings.delegates.GenresValidator
import com.hellmund.primetime.ui.settings.delegates.StreamingServicesDelegate
import com.hellmund.primetime.ui.settings.delegates.ValidationResult
import com.hellmund.primetime.utils.Constants
import com.hellmund.primetime.utils.openUrl
import com.hellmund.primetime.utils.showInfoDialog
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var genresRepository: GenresRepository

    @Inject
    lateinit var streamingServicesStore: StreamingServicesStore

    @Inject
    lateinit var genresDelegate: GenresDelegate

    @Inject
    lateinit var streamingServicesDelegate: StreamingServicesDelegate

    @Inject
    lateinit var genresValidator: GenresValidator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injector.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        
        initIncludedGenresPref()
        initExcludedGenresPref()
        initStreamingServicesPref()
        initRateAppPref()
        initAboutAppPref()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    private fun initIncludedGenresPref() {
        val preference = requirePreference<MultiSelectListPreference>(Constants.KEY_INCLUDED)
        genresDelegate.init(preference)

        preference.onPreferenceChangeListener = OnPreferenceChangeListener { pref, newValue ->
            saveGenresSelection(pref, newValue)
        }
    }

    private fun initExcludedGenresPref() {
        val preference = requirePreference<MultiSelectListPreference>(Constants.KEY_EXCLUDED)
        genresDelegate.init(preference)

        preference.onPreferenceChangeListener = OnPreferenceChangeListener { pref, newValue ->
            saveGenresSelection(pref, newValue)
        }
    }

    private fun initStreamingServicesPref() {
        val preference = requirePreference<MultiSelectListPreference>(Constants.KEY_STREAMING_SERVICES)
        streamingServicesDelegate.init(preference)

        preference.onPreferenceChangeListener = OnPreferenceChangeListener { pref, newValue ->
            saveStreamingServices(pref, newValue)
        }
    }

    private fun saveGenresSelection(pref: Preference, newValue: Any): Boolean {
        val result = genresValidator.validate(pref, newValue)
        return when (result) {
            is ValidationResult.Success -> {
                genresDelegate.updateGenresSummary(pref, result.genres)
                true
            }
            is ValidationResult.NotEnough -> {
                displayNotEnoughCheckedAlert()
                false
            }
            is ValidationResult.Overlap -> {
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

    private fun saveStreamingServices(pref: Preference, newValue: Any): Boolean {
        val values = newValue as Set<String>
        val services = streamingServicesStore.all

        val updatedServices = services.map { it.copy(isSelected = values.contains(it.name)) }
        streamingServicesDelegate.updateStreamingServicesSummary(pref, values)

        streamingServicesStore.store(updatedServices)
        return true
    }

    private fun initRateAppPref() {
        val ratePrimeTime = requirePreference<Preference>(Constants.KEY_PLAY_STORE)

        ratePrimeTime.setOnPreferenceClickListener { preference ->
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
        val about = requirePreference<Preference>(Constants.KEY_ABOUT)

        val version = getVersionName()
        about.summary = version?.let { "Version $it" }

        about.setOnPreferenceClickListener {
            val intent = AboutActivity.newIntent(requireContext())
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
