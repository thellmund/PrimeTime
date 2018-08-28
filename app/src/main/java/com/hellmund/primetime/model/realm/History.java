package com.hellmund.primetime.model.realm;

import com.hellmund.primetime.model.HistoryMovie;
import com.hellmund.primetime.model.Movie;
import com.hellmund.primetime.model.Sample;
import com.hellmund.primetime.model.SearchResult;
import com.hellmund.primetime.model.WatchlistMovie;
import com.hellmund.primetime.utils.Constants;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class History {

    public static ArrayList<HistoryMovie> get() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<RealmHistoryMovie> history = realm
                .where(RealmHistoryMovie.class)
                .sort("timestamp", Sort.DESCENDING).findAll();

        final int length = history.size();
        ArrayList<HistoryMovie> results = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            results.add(new HistoryMovie(history.get(i)));
        }

        realm.close();

        return results;
    }

    public static ArrayList<HistoryMovie> getLatestLiked() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<RealmHistoryMovie> history = realm
                .where(RealmHistoryMovie.class)
                .equalTo("rating", Constants.LIKE)
                .sort("timestamp", Sort.DESCENDING)
                .findAll();

        final int length = Math.min(10, history.size());
        ArrayList<HistoryMovie> results = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            results.add(new HistoryMovie(history.get(i)));
        }

        realm.close();

        return results;
    }

    public static boolean contains(int id) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(RealmHistoryMovie.class).equalTo("id", id).findFirst() != null;
    }

    public static int getRating(int id) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(RealmHistoryMovie.class).equalTo("id", id).findFirst().getRating();
    }

    public static void changeRating(int id, int newRating) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(r -> {
            r.where(RealmHistoryMovie.class).equalTo("id", id).findFirst().setRating(newRating);
        });
    }

    public static void add(Movie movie, int rating) {
        add(movie.getID(), movie.getTitle(), rating);
    }

    public static void addHistoryMovie(HistoryMovie movie) {
        add(movie.getID(), movie.getTitle(), movie.getRating());
    }

    public static void add(WatchlistMovie movie, int rating) {
        add(movie.getID(), movie.getTitle(), rating);
    }

    public static void add(SearchResult sample, int rating) {
        add(sample.getID(), sample.getTitle(), rating);
    }

    private static void add(int id, String title, int rating) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(r -> {
            if (!contains(id)) {
                RealmHistoryMovie movie = r.createObject(RealmHistoryMovie.class);
                movie.setId(id);
                movie.setTitle(title);
                movie.setRating(rating);
                movie.setTimestamp(System.currentTimeMillis());
            }
        });
    }

    public static void addAll(ArrayList<HistoryMovie> movies) {
        if (movies == null) {
            return;
        }

        for (int i = 0; i < movies.size(); i++) {
            addHistoryMovie(movies.get(i));
        }
    }

    public static void addSamples(ArrayList<Sample> samples) {
        for (int i = 0; i < samples.size(); i++) {
            add(samples.get(i).getID(), samples.get(i).getTitle(), Constants.LIKE);
        }
    }

    public static void remove(int id) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(r -> {
            r.where(RealmHistoryMovie.class).equalTo("id", id).findFirst().deleteFromRealm();
        });
    }

}
