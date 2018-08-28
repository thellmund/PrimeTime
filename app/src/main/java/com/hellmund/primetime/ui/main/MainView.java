package com.hellmund.primetime.ui.main;

interface MainView {

    void onDownloadStart();

    void onSuccess();
    void onError();
    void onEmpty();

    void onMovieRatingAdded(int id, int rating);
    void tryDownloadAgain();
    void openGenresDialog();
    void openSearch();
    void openWatchlist();

}
