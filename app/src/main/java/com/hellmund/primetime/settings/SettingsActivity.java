package com.hellmund.primetime.settings;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.hellmund.primetime.R;
import com.hellmund.primetime.about.AboutActivity;
import com.hellmund.primetime.utils.Constants;
import com.hellmund.primetime.utils.GenreUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.content, SettingsFragment.newInstance(this))
                    .commit();
        }

        initToolbar();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment {

        private static final int MIN_GENRES = 2;

        private Context mContext;
        private ArrayMap<Preference, String> mDefaultSummaries;

        public static SettingsFragment newInstance(Context context) {
            SettingsFragment fragment = new SettingsFragment();
            fragment.mContext = context;
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setRetainInstance(true);

            addPreferencesFromResource(R.xml.preferences);

            initDefaultSummaries();
            initIncludedGenresPref();
            initExcludedGenresPref();
            initRateAppPref();
            initAboutAppPref();
        }

        private void initRateAppPref() {
            Preference ratePrimeTime = findPreference(Constants.KEY_PLAY_STORE);
            ratePrimeTime.setOnPreferenceClickListener(preference -> {
                openPlayStore();
                return true;
            });
        }

        private void initAboutAppPref() {
            Preference about = findPreference(Constants.KEY_ABOUT);
            final String version = getVersionName();
            about.setSummary((version != null) ? "Version " + getVersionName() : null);
            about.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                startActivity(intent);
                return true;
            });
        }

        private void initExcludedGenresPref() {
            MultiSelectListPreference excludedGenres =
                    (MultiSelectListPreference) findPreference(Constants.KEY_EXCLUDED);

            excludedGenres.setEntries(GenreUtils.getGenreNames(getActivity()));
            excludedGenres.setEntryValues(GenreUtils.getGenreIds(getActivity()));
            updateGenresSummary(excludedGenres, excludedGenres.getValues());

            excludedGenres.setOnPreferenceChangeListener(this::saveExcludedGenres);
        }

        @SuppressWarnings("unchecked")
        private boolean saveIncludedGenres(Preference pref, Object newValue) {
            if (enoughGenresChecked(newValue) && genresAreDisjoint(pref, newValue)) {
                updateGenresSummary(pref, (Set<String>) newValue);
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
                updateGenresSummary(pref, (Set<String>) newValue);
                return true;
            } else {
                displaySharedGenresAlert(pref, (Set<String>) newValue);
                return false;
            }
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

        @SuppressWarnings("unchecked")
        private void initIncludedGenresPref() {
            MultiSelectListPreference included =
                    (MultiSelectListPreference) findPreference(Constants.KEY_INCLUDED);

            included.setEntries(GenreUtils.getGenreNames(getActivity()));
            included.setEntryValues(GenreUtils.getGenreIds(getActivity()));
            updateGenresSummary(included, included.getValues());

            included.setOnPreferenceChangeListener(this::saveIncludedGenres);
        }

        private String getSharedGenresAsBulletList(Preference preference, Set<String> newValues) {
            Set<String> included;
            Set<String> excluded;

            if (preference.getKey().equals(Constants.KEY_INCLUDED)) {
                included = newValues;
                excluded = GenreUtils.getExcludedGenres(mContext);
            } else {
                included = GenreUtils.getPreferredGenres(mContext);
                excluded = newValues;
            }

            Set<String> sharedGenres = new HashSet<>(included);
            sharedGenres.retainAll(excluded);

            List<String> sharedTitles = new ArrayList<>();
            for (String genreId : sharedGenres) {
                String value = "• " + GenreUtils.getGenreName(getActivity(), Integer.parseInt(genreId));
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
        }

        private void displayAlertDialog(String text) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(text)
                    .setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss()).create().show();
        }

        @SuppressWarnings("unchecked")
        private boolean genresAreDisjoint(Preference preference, Object newValues) {
            Set<String> included;
            Set<String> excluded;

            if (preference.getKey().equals(Constants.KEY_INCLUDED)) {
                included = (Set<String>) newValues;
                excluded = GenreUtils.getExcludedGenres(mContext);
            } else {
                included = GenreUtils.getPreferredGenres(mContext);
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

        private String getGenresAsString(Set<String> values) {
            List<String> genreIds = new ArrayList<>(values);
            List<String> genreNames = new ArrayList<>();

            for (String genreId : genreIds) {
                genreNames.add(GenreUtils.getGenreName(getActivity(), Integer.parseInt(genreId)));
            }

            StringBuilder builder = new StringBuilder(genreNames.get(0));
            for (int i = 1; i < genreNames.size(); i++) {
                builder.append(", ").append(genreNames.get(i));
            }

            return builder.toString();
        }

        private String getVersionName() {
            try {
                return getActivity().getPackageManager()
                                    .getPackageInfo(getActivity().getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                return null;
            }
        }

        private void openPlayStore() {
            final String packageName = getActivity().getPackageName();
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

}
