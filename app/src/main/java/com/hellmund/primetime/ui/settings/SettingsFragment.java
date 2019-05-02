package com.hellmund.primetime.ui.settings;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.hellmund.primetime.App;
import com.hellmund.primetime.R;
import com.hellmund.primetime.data.model.Genre;
import com.hellmund.primetime.ui.about.AboutActivity;
import com.hellmund.primetime.ui.selectgenres.GenresRepository;
import com.hellmund.primetime.ui.selectstreamingservices.StreamingService;
import com.hellmund.primetime.ui.selectstreamingservices.StreamingServicesStore;
import com.hellmund.primetime.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import static androidx.core.util.Preconditions.checkNotNull;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int MIN_GENRES = 2;

    @Inject
    GenresRepository genresRepository;

    @Inject
    StreamingServicesStore streamingServicesStore;

    private ArrayMap<Preference, String> mDefaultSummaries;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        App app = (App) context.getApplicationContext();
        app.getAppComponent().inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        initDefaultSummaries();
        initIncludedGenresPref();
        initExcludedGenresPref();
        initStreamingServicesPref();
        initRateAppPref();
        initAboutAppPref();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    private void initRateAppPref() {
        Preference ratePrimeTime = findPreference(Constants.KEY_PLAY_STORE);
        checkNotNull(ratePrimeTime);

        ratePrimeTime.setOnPreferenceClickListener(preference -> {
            openPlayStore();
            return true;
        });
    }

    private void initAboutAppPref() {
        Preference about = findPreference(Constants.KEY_ABOUT);
        checkNotNull(about);

        final String version = getVersionName();
        about.setSummary((version != null) ? "Version " + getVersionName() : null);
        about.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity(), AboutActivity.class);
            startActivity(intent);
            return true;
        });
    }

    private void initExcludedGenresPref() {
        MultiSelectListPreference preference = findPreference(Constants.KEY_EXCLUDED);
        checkNotNull(preference);

        List<Genre> genres = genresRepository.getAll().blockingGet();
        List<Genre> excludedGenres = genresRepository.getExcludedGenres().blockingFirst();

        Set<String> values = new HashSet<>();
        for (Genre genre : excludedGenres) {
            if (genre.isExcluded()) {
                values.add(Integer.toString(genre.getId()));
            }
        }

        String[] genreNames = new String[genres.size()];
        String[] genreIds = new String[genres.size()];

        for (int i = 0; i < genres.size(); i++) {
            genreNames[i] = genres.get(i).getName();
            genreIds[i] = Integer.toString(genres.get(i).getId());
        }

        preference.setEntries(genreNames);
        preference.setEntryValues(genreIds);
        preference.setValues(values);

        updateGenresSummary(preference, values);
        preference.setOnPreferenceChangeListener(this::saveExcludedGenres);
    }

    private void initStreamingServicesPref() {
        MultiSelectListPreference preference = findPreference(Constants.KEY_STREAMING_SERVICES);
        checkNotNull(preference);

        List<StreamingService> streamingServices = streamingServicesStore.getAll();
        Set<String> values = new HashSet<>();
        for (StreamingService streamingService : streamingServices) {
            if (streamingService.isSelected()) {
                values.add(streamingService.getName());
            }
        }

        String[] entries = new String[streamingServices.size()];

        for (int i = 0; i < streamingServices.size(); i++) {
            entries[i] = streamingServices.get(i).getName();
        }

        preference.setEntries(entries);
        preference.setEntryValues(entries);
        preference.setValues(values);

        updateStreamingServicesSummary(preference, values);
        preference.setOnPreferenceChangeListener(this::saveStreamingServices);
    }

    @SuppressWarnings("unchecked")
    private boolean saveIncludedGenres(Preference pref, Object newValue) {
        if (enoughGenresChecked(newValue) && genresAreDisjoint(pref, newValue)) {
            Set<String> genreIds = (Set<String>) newValue;
            List<Genre> genres = getGenresFromValues(genreIds);

            for (Genre genre : genres) {
                genre.setPreferred(true);
                genre.setExcluded(false);
            }

            genresRepository.storeGenres(genres);
            updateGenresSummary(pref, genreIds);
            return true;
        } else if (!enoughGenresChecked(newValue)) {
            displayNotEnoughCheckedAlert();
            return false;
        } else {
            displaySharedGenresAlert(pref, (Set<String>) newValue);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean saveExcludedGenres(Preference pref, Object newValue) {
        if (genresAreDisjoint(pref, newValue)) {
            Set<String> genreIds = (Set<String>) newValue;
            List<Genre> genres = getGenresFromValues(genreIds);

            for (Genre genre : genres) {
                genre.setPreferred(false);
                genre.setExcluded(true);
            }

            genresRepository.storeGenres(genres);
            updateGenresSummary(pref, genreIds);
            return true;
        } else {
            displaySharedGenresAlert(pref, (Set<String>) newValue);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean saveStreamingServices(Preference pref, Object newValue) {
        Set<String> values = (Set<String>) newValue;
        List<StreamingService> services = streamingServicesStore.getAll();

        List<StreamingService> updatedServices = new ArrayList<>();

        for (StreamingService service : services) {
            boolean isSelected = values.contains(service.getName());
            StreamingService newService = new StreamingService(service.getName(), isSelected);
            updatedServices.add(newService);
        }

        updateStreamingServicesSummary(pref, values);
        streamingServicesStore.store(updatedServices);
        return true;
    }

    private List<Genre> getGenresFromValues(Set<String> values) {
        List<Genre> results = new ArrayList<>();
        for (String value : values) {
            Genre genre = genresRepository.getGenre(value).blockingGet();
            results.add(genre);
        }
        return results;
    }

    private void displaySharedGenresAlert(Preference pref, Set<String> newValue) {
        final String sharedGenres = getSharedGenresAsBulletList(pref, newValue);
        final String error = String.format(
                getString(R.string.error_already_in_genres), sharedGenres);
        displayAlertDialog(error);
    }

    private void displayNotEnoughCheckedAlert() {
        displayAlertDialog(getString(R.string.need_more_genres));
    }

    private void initIncludedGenresPref() {
        MultiSelectListPreference preference = findPreference(Constants.KEY_INCLUDED);
        checkNotNull(preference);

        List<Genre> genres = genresRepository.getAll().blockingGet();
        List<Genre> includedGenres = genresRepository.getPreferredGenres().blockingFirst();

        Set<String> values = new HashSet<>();
        for (Genre genre : includedGenres) {
            if (genre.isPreferred()) {
                values.add(Integer.toString(genre.getId()));
            }
        }

        String[] genreNames = new String[genres.size()];
        String[] genreIds = new String[genres.size()];

        for (int i = 0; i < genres.size(); i++) {
            genreNames[i] = genres.get(i).getName();
            genreIds[i] = Integer.toString(genres.get(i).getId());
        }

        preference.setEntries(genreNames);
        preference.setEntryValues(genreIds);
        preference.setValues(values);

        updateGenresSummary(preference, values);
        preference.setOnPreferenceChangeListener(this::saveIncludedGenres);
    }

    private String getSharedGenresAsBulletList(Preference preference, Set<String> newValues) {
        Set<String> included;
        Set<String> excluded;

        if (preference.getKey().equals(Constants.KEY_INCLUDED)) {
            included = newValues;

            List<Genre> excludedGenres = genresRepository.getExcludedGenres().blockingFirst();
            excluded = new HashSet<>();

            for (Genre genre : excludedGenres) {
                excluded.add(Integer.toString(genre.getId()));
            }
        } else {
            excluded = newValues;

            List<Genre> includedGenres = genresRepository.getPreferredGenres().blockingFirst();
            included = new HashSet<>();

            for (Genre genre : includedGenres) {
                included.add(Integer.toString(genre.getId()));
            }
        }

        Set<String> sharedGenres = new HashSet<>(included);
        sharedGenres.retainAll(excluded);

        List<String> sharedTitles = new ArrayList<>();
        for (String genreId : sharedGenres) {
            Genre genre = genresRepository.getGenre(genreId).blockingGet();
            String value = "• " + genre.getName();
            sharedTitles.add(value);
        }

        Collections.sort(sharedTitles);
        int length = sharedTitles.size();

        StringBuilder builder = new StringBuilder(sharedTitles.get(0));

        for (int i = 1; i < length; i++) {
            builder.append("\n").append(sharedTitles.get(i));
        }
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private boolean enoughGenresChecked(Object newValues) {
        Set<String> genres = (Set<String>) newValues;
        return genres.size() >= MIN_GENRES;
    }

    private void initDefaultSummaries() {
        mDefaultSummaries = new ArrayMap<>();
        mDefaultSummaries.put(findPreference(Constants.KEY_INCLUDED),
                getString(R.string.preferred_genres_summary));
        mDefaultSummaries.put(findPreference(Constants.KEY_EXCLUDED),
                getString(R.string.excluded_genres_summary));
        mDefaultSummaries.put(findPreference(Constants.KEY_STREAMING_SERVICES),
                getString(R.string.streaming_services_summary));
    }

    private void displayAlertDialog(String text) {
        new AlertDialog.Builder(getActivity())
                .setMessage(text)
                .setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss()).create().show();
    }

    @SuppressWarnings("unchecked")
    private boolean genresAreDisjoint(Preference preference, Object newValues) {
        Set<String> included = new HashSet<>();
        Set<String> excluded = new HashSet<>();

        List<Genre> includedGenres = genresRepository.getPreferredGenres().blockingFirst();
        List<Genre> excludedGenres = genresRepository.getExcludedGenres().blockingFirst();

        for (Genre genre : includedGenres) {
            included.add(Integer.toString(genre.getId()));
        }

        for (Genre genre : excludedGenres) {
            excluded.add(Integer.toString(genre.getId()));
        }

        if (preference.getKey().equals(Constants.KEY_INCLUDED)) {
            included = (Set<String>) newValues;
        } else {
            excluded = (Set<String>) newValues;
        }

        return excluded == null || Collections.disjoint(included, excluded);
    }

    private void updateGenresSummary(Preference preference, Set<String> values) {
        if (!values.isEmpty()) {
            final String genres = getGenresAsString(values);
            preference.setSummary(genres);
        } else {
            preference.setSummary(mDefaultSummaries.get(preference));
        }
    }

    private void updateStreamingServicesSummary(Preference preference, Set<String> values) {
        if (!values.isEmpty()) {
            List<StreamingService> services = streamingServicesStore.getAll();
            List<String> selected = new ArrayList<>();

            for (String value : values) {
                for (StreamingService service : services) {
                    if (service.getName().equals(value)) {
                        selected.add(value);
                    }
                }
            }

            Collections.sort(selected, (lhs, rhs) -> lhs.toLowerCase().compareTo(rhs.toLowerCase()));

            StringBuilder builder = new StringBuilder(selected.get(0));
            for (int i = 1; i < selected.size(); i++) {
                builder.append(", ").append(selected.get(i));
            }
            preference.setSummary(builder.toString());
        } else {
            preference.setSummary(mDefaultSummaries.get(preference));
        }
    }

    private String getGenresAsString(Set<String> values) {
        List<String> genreIds = new ArrayList<>(values);
        List<String> genreNames = new ArrayList<>();

        for (String genreId : genreIds) {
            Genre genre = genresRepository.getGenre(genreId).blockingGet();
            genreNames.add(genre.getName());
        }

        Collections.sort(genreNames);

        StringBuilder builder = new StringBuilder(genreNames.get(0));
        for (int i = 1; i < genreNames.size(); i++) {
            builder.append(", ").append(genreNames.get(i));
        }

        return builder.toString();
    }

    private String getVersionName() {
        try {
            return requireActivity().getPackageManager()
                    .getPackageInfo(requireActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private void openPlayStore() {
        final String packageName = requireActivity().getPackageName();
        Intent intent;

        try {
            intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + packageName));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            startActivity(intent);
        }
    }

}
