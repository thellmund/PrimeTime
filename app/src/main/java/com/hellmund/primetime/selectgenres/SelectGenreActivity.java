package com.hellmund.primetime.selectgenres;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.SparseBooleanArray;
import android.widget.ListView;

import com.hellmund.primetime.R;
import com.hellmund.primetime.model2.Genre;
import com.hellmund.primetime.selectmovies.SelectMoviesActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class SelectGenreActivity extends AppCompatActivity {

    private final static int MIN_COUNT = 2;

    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list_view) ListView mListView;
    @BindView(R.id.button) AppCompatButton mSaveButton;

    private SelectGenresViewModel viewModel;
    private List<Genre> genres;

    @Inject
    Provider<SelectGenresViewModel> viewModelProvider;

    public static Intent newIntent(Context context) {
        return new Intent(context, SelectGenreActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_genre);


        getWindow().setBackgroundDrawable(null);
        ButterKnife.bind(this);

        // AppDatabase db = PrimeTimeDatabase.getInstance(this);
        // GenresRepository repository = new GenresRepository(ApiClient.getInstance(), db);
        // SelectGenresViewModel.Factory factory = new SelectGenresViewModel.Factory(repository);

        viewModel = viewModelProvider.get();
        // viewModel = ViewModelProviders.of(this, factory).get(SelectGenresViewModel.class);
        viewModel.getViewState().observe(this, this::render);

        // TODO
        // When selecting the first two genres, slide in the next button from the bottom
    }

    private void render(SelectGenresViewState viewState) {
        genres = viewState.getData();
        swipeRefreshLayout.setRefreshing(viewState.isLoading());
        swipeRefreshLayout.setEnabled(false);
        showGenres(viewState.getData());

        // TODO: Error and loading handling (SwipeRefreshLayout)
    }

    private void showGenres(List<Genre> genres) {
        mListView.setAdapter(new GenresAdapter(this, genres));
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

        List<Genre> includedGenres = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            final Genre genre = genres.get(i);
            genre.setPreferred(checkedItems.get(i));
            includedGenres.add(genre);
        }

        viewModel.store(includedGenres);
    }

    private void openMoviesSelection() {
        Intent intent = new Intent(this, SelectMoviesActivity.class);
        startActivity(intent);
    }

}
