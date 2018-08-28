package com.hellmund.primetime.utils;

import android.content.Context;

import com.hellmund.primetime.model.Movie;
import com.hellmund.primetime.model.realm.History;
import com.hellmund.primetime.model.realm.Watchlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class ResultBuilder {

    private ArrayList<Movie> movies;
    private Set<String> excludedGenres;

    ResultBuilder removeUnreleasedAndKnown() {
        ArrayList<Movie> result = new ArrayList<>();

        for (Movie movie : movies) {
            if (isUnknown(movie.getID()) && !movie.isUnreleased()) {
                result.add(movie);
            }
        }

        movies = result;
        return this;
    }

    private boolean isUnknown(int id) {
        return !Watchlist.contains(id) && !History.contains(id);
    }

    ResultBuilder removeKnown() {
        ArrayList<Movie> result = new ArrayList<>();

        for (Movie movie : movies) {
            if (isUnknown(movie.getID())) {
                result.add(movie);
            }
        }

        movies = result;
        return this;
    }

    ResultBuilder removeExcludedGenres() {
        if (!excludedGenres.isEmpty()) {
            ArrayList<Movie> result = new ArrayList<>();

            for (Movie movie : movies) {
                if (!hasExcludedGenres(excludedGenres, movie)) {
                    result.add(movie);
                }
            }

            movies = result;
        }
        return this;
    }

    private boolean hasExcludedGenres(Set<String> excluded, Movie movie) {
        final int[] ids = movie.getGenreIDs();

        for (int id : ids) {
            if (excluded.contains(Integer.toString(id))) {
                return true;
            }
        }

        return false;
    }

    ResultBuilder removeDuplicates() {
        Set<Integer> ids = new HashSet<>();

        ArrayList<Movie> result = new ArrayList<>();
        for (Movie movie : movies) {
            final boolean insert = !ids.contains(movie.getID());
            if (insert) {
                result.add(movie);
                ids.add(movie.getID());
            }
        }

        movies = result;
        return this;
    }

    ResultBuilder sort() {
        Collections.sort(movies);
        return this;
    }

    ArrayList<Movie> build() {
        return movies;
    }

    ResultBuilder(Context context, ArrayList<Movie> movies) {
        this.movies = movies;
        this.excludedGenres = GenreUtils.getExcludedGenres(context);
    }

}
