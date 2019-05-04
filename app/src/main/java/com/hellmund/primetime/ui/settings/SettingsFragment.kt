package com.hellmund.primetime.ui.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import com.hellmund.primetime.utils.Constants
import com.hellmund.primetime.utils.showInfoDialog
import java.util.*
import javax.inject.Inject

private const val MIN_GENRES = 2

class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var genresRepository: GenresRepository

    @Inject
    lateinit var streamingServicesStore: StreamingServicesStore

    private val defaultSummaries = mutableMapOf<Preference, String>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injector.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        initDefaultSummaries()
        initIncludedGenresPref()
        initExcludedGenresPref()
        initStreamingServicesPref()
        initRateAppPref()
        initAboutAppPref()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    private fun initDefaultSummaries() {
        val included = checkNotNull(findPreference<Preference>(Constants.KEY_INCLUDED))
        defaultSummaries[included] = getString(R.string.preferred_genres_summary)

        val excluded = checkNotNull(findPreference<Preference>(Constants.KEY_EXCLUDED))
        defaultSummaries[excluded] = getString(R.string.excluded_genres_summary)

        val streaming = checkNotNull(findPreference<Preference>(Constants.KEY_STREAMING_SERVICES))
        defaultSummaries[streaming] = getString(R.string.streaming_services_summary)
    }

    private fun initIncludedGenresPref() {
        val preference = checkNotNull(findPreference<MultiSelectListPreference>(Constants.KEY_INCLUDED))

        val genres = genresRepository.all.blockingGet()
        val includedGenres = genresRepository.preferredGenres.blockingFirst()

        val values = includedGenres
                .filter { it.isPreferred }
                .map { it.id.toString() }
                .toSet()

        val genreIds = genres.map { it.id.toString() }
        val genreNames = genres.map { it.name }

        preference.entries = genreNames.toTypedArray()
        preference.entryValues = genreIds.toTypedArray()
        preference.values = values

        updateGenresSummary(preference, values)
        preference.onPreferenceChangeListener = OnPreferenceChangeListener { pref, newValue ->
            saveIncludedGenres(pref, newValue)
        }
    }

    private fun initExcludedGenresPref() {
        val preference = checkNotNull(findPreference<MultiSelectListPreference>(Constants.KEY_EXCLUDED))

        val genres = genresRepository.all.blockingGet()
        val values = genresRepository.excludedGenres.blockingFirst()
                .filter { it.isExcluded }
                .map { it.id.toString() }
                .toSet()

        val genreIds = genres.map { it.id.toString() }
        val genreNames = genres.map { it.name }

        preference.entries = genreNames.toTypedArray()
        preference.entryValues = genreIds.toTypedArray()
        preference.values = values

        updateGenresSummary(preference, values)
        preference.onPreferenceChangeListener = OnPreferenceChangeListener { pref, newValue ->
            saveExcludedGenres(pref, newValue)
        }
    }

    private fun initStreamingServicesPref() {
        val preference = checkNotNull(findPreference<MultiSelectListPreference>(Constants.KEY_STREAMING_SERVICES))

        val streamingServices = streamingServicesStore.all
        val values = streamingServices
                .filter { it.isSelected }
                .map { it.name }
                .toSet()

        val entries = streamingServices.map { it.name }

        preference.entries = entries.toTypedArray()
        preference.entryValues = entries.toTypedArray()
        preference.values = values

        updateStreamingServicesSummary(preference, values)
        preference.onPreferenceChangeListener = OnPreferenceChangeListener { pref, newValue ->
            saveStreamingServices(pref, newValue)
        }
    }

    private fun saveIncludedGenres(pref: Preference, newValue: Any): Boolean {
        val genreIds = newValue as Set<String>

        if (enoughGenresChecked(genreIds) && genresAreDisjoint(pref, newValue)) {
            val genres = getGenresFromValues(genreIds)

            for (genre in genres) {
                genre.isPreferred = true
                genre.isExcluded = false
            }

            genresRepository.storeGenres(genres)
            updateGenresSummary(pref, genreIds)
            return true
        } else if (!enoughGenresChecked(newValue)) {
            displayNotEnoughCheckedAlert()
            return false
        } else {
            displaySharedGenresAlert(pref, genreIds)
            return false
        }
    }

    private fun enoughGenresChecked(newGenres: Set<String>): Boolean {
        return newGenres.size >= MIN_GENRES
    }

    private fun displayNotEnoughCheckedAlert() {
        requireContext().showInfoDialog(R.string.need_more_genres)
    }

    private fun genresAreDisjoint(preference: Preference, newGenres: Set<String>): Boolean {
        val includedGenres = genresRepository.preferredGenres.blockingFirst()
        val excludedGenres = genresRepository.excludedGenres.blockingFirst()

        val includedIds = includedGenres.map { it.id.toString() }.toMutableSet()
        val excludedIds = excludedGenres.map { it.id.toString() }.toMutableSet()

        if (preference.key == Constants.KEY_INCLUDED) {
            includedIds.clear()
            includedIds += newGenres
        } else {
            excludedIds.clear()
            excludedIds += newGenres
        }

        return excludedIds.isEmpty() || Collections.disjoint(includedIds, excludedIds)
    }

    private fun saveExcludedGenres(pref: Preference, newValue: Any): Boolean {
        val genreIds = newValue as Set<String>

        if (genresAreDisjoint(pref, genreIds)) {
            val genres = getGenresFromValues(genreIds)

            for (genre in genres) {
                genre.isPreferred = false
                genre.isExcluded = true
            }

            genresRepository.storeGenres(genres)
            updateGenresSummary(pref, genreIds)
            return true
        } else {
            displaySharedGenresAlert(pref, genreIds)
            return false
        }
    }

    private fun getGenresFromValues(values: Set<String>): List<Genre> {
        return values
                .map { genresRepository.getGenre(it).blockingGet() }
                .sortedBy { it.name }
    }

    private fun displaySharedGenresAlert(pref: Preference, newValue: Set<String>) {
        val sharedGenres = getSharedGenresAsBulletList(pref, newValue)
        val error = getString(R.string.error_already_in_genres, sharedGenres)
        requireContext().showInfoDialog(error)
    }

    private fun getSharedGenresAsBulletList(preference: Preference, newValues: Set<String>): String {
        val included: Set<String>
        val excluded: Set<String>

        if (preference.key == Constants.KEY_INCLUDED) {
            included = newValues

            val excludedGenres = genresRepository.excludedGenres.blockingFirst()
            excluded = excludedGenres.map { it.id.toString() }.toSet()
        } else {
            excluded = newValues

            val includedGenres = genresRepository.preferredGenres.blockingFirst()
            included = includedGenres.map { it.id.toString() }.toSet()
        }

        val sharedGenres = HashSet(included)
        sharedGenres.retainAll(excluded)

        return sharedGenres
                .map { genresRepository.getGenre(it).blockingGet() }
                .map { "• ${it.name}" }
                .sorted()
                .joinToString("\n")
    }

    private fun saveStreamingServices(pref: Preference, newValue: Any): Boolean {
        val values = newValue as Set<String>
        val services = streamingServicesStore.all

        val updatedServices = services.map { it.copy(isSelected = values.contains(it.name)) }
        updateStreamingServicesSummary(pref, values)

        streamingServicesStore.store(updatedServices)
        return true
    }

    private fun updateGenresSummary(preference: Preference, values: Set<String>) {
        if (values.isNotEmpty()) {
            preference.summary = joinGenresToString(values)
        } else {
            preference.summary = defaultSummaries[preference]
        }
    }

    private fun updateStreamingServicesSummary(preference: Preference, values: Set<String>) {
        if (values.isNotEmpty()) {
            val summary = streamingServicesStore.all
                    .filter { it.name in values }
                    .map { it.name }
                    .sortedBy { it.toLowerCase() }
                    .joinToString(", ")
            preference.summary = summary
        } else {
            preference.summary = defaultSummaries[preference]
        }
    }

    private fun joinGenresToString(values: Set<String>): String {
        val genreIds = values.toList()
        return genreIds
                .map { genresRepository.getGenre(it).blockingGet() }
                .map { it.name }
                .sorted()
                .joinToString(", ")
    }

    private fun initRateAppPref() {
        val ratePrimeTime = findPreference<Preference>(Constants.KEY_PLAY_STORE)
        checkNotNull(ratePrimeTime)

        ratePrimeTime.setOnPreferenceClickListener { preference ->
            openPlayStore()
            true
        }
    }

    private fun openPlayStore() {
        val packageName = requireActivity().packageName

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            startActivity(intent)
        }
    }

    private fun initAboutAppPref() {
        val about = checkNotNull(findPreference<Preference>(Constants.KEY_ABOUT))

        val version = getVersionName()
        about.summary = if (version != null) "Version " + getVersionName()!! else null
        about.setOnPreferenceClickListener { preference ->
            val intent = Intent(activity, AboutActivity::class.java)
            startActivity(intent)
            true
        }
    }

    private fun getVersionName(): String? {
        return try {
            requireActivity().packageManager
                    .getPackageInfo(requireActivity().packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }

}
