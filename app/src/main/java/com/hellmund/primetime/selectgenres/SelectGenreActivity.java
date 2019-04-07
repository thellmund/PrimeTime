package com.hellmund.primetime.selectgenres;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.hellmund.primetime.R;
import com.hellmund.primetime.model.Genre;
import com.hellmund.primetime.selectmovies.SelectMoviesActivity;
import com.hellmund.primetime.utils.Constants;
import com.hellmund.primetime.utils.GenreUtils;

import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class SelectGenreActivity extends Activity {

    private final static int MIN_COUNT = 2;

    @BindView(R.id.list_view) ListView mListView;
    @BindView(R.id.button) AppCompatButton mSaveButton;

    private Genre[] mGenres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_genre);

        getWindow().setBackgroundDrawable(null);
        ButterKnife.bind(this);

        setListContent();

        // TODO
        // When selecting the first two genres, slide in the next button from the bottom
    }

    private void setListContent() {
        mGenres = GenreUtils.getGenres(this);

        ArrayAdapter<Genre> adapter = new ArrayAdapter<Genre>(
                getApplicationContext(), R.layout.list_item_multiple_choice, mGenres) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(SelectGenreActivity.this)
                            .inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
                }

                CheckedTextView textView = convertView.findViewById(android.R.id.text1);
                textView.setText(mGenres[position].getName());
                textView.setTextColor(
                        ContextCompat.getColor(SelectGenreActivity.this, android.R.color.white));

                return convertView;
            }
        };

        mListView.setAdapter(adapter);
    }

    @OnItemClick(R.id.list_view)
    void onItemClick() {
        final boolean isEnabled = mListView.getCheckedItemCount() >= MIN_COUNT;
        mSaveButton.setClickable(isEnabled);
        mSaveButton.setEnabled(isEnabled);
    }

    @OnClick(R.id.button)
    public void saveGenreSelection() {
        saveGenres();
        openMoviesSelection();
    }

    private void saveGenres() {
        final int length = mListView.getCount();
        SparseBooleanArray checkedItems = mListView.getCheckedItemPositions();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> genres = sharedPrefs.getStringSet(Constants.KEY_INCLUDED, new HashSet<>());

        for (int i = 0; i < length; i++) {
            if (checkedItems.get(i)) {
                final int genreID = mGenres[i].getId();
                genres.add(Integer.toString(genreID));
            }
        }

        sharedPrefs.edit().putStringSet(Constants.KEY_INCLUDED, genres).apply();
    }

    private void openMoviesSelection() {
        Intent intent = new Intent(this, SelectMoviesActivity.class);
        startActivity(intent);
    }

}
