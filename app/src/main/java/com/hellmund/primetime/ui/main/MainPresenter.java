package com.hellmund.primetime.ui.main;

import com.hellmund.primetime.model.HistoryMovie;
import com.hellmund.primetime.model.Movie;

import java.util.ArrayList;

interface MainPresenter {

    void loadIndices();

    void handleShortcutOpen(String intent);

    void saveIndices();

    String getToolbarSubtitle();

    ArrayList<Movie> getRecommendations();

    void setRecommendations(ArrayList<Movie> recommendations);

    Movie getMovieAt(int position);

    boolean genreAlreadySelected(String selected);

    void handleGenreDialogInput(String selected, int which);

    ArrayList<HistoryMovie> getHistory();

    void setupSingleMovieRecommendations(int id, String title);

    void addToWatchlist(int position);

    void saveInWatchlistOnDevice(Movie movie);

    void addMovieRating(int position, int rating);

    void removeFromWatchlist(int id);

    void forceRecommendationsDownload();

    void downloadHistoryAndRecommendations();

    void showUndoToast(int id, int rating);

    boolean onWatchlist(int id);

}
