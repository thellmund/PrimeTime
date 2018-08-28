package com.hellmund.primetime.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.hellmund.primetime.R;
import com.hellmund.primetime.utils.DateUtils;
import com.hellmund.primetime.utils.GenreUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Movie implements Parcelable, Comparable<Movie> {

    private int id;
    private String posterURL;
    private String title;
    private int[] genreIds;
    private String description;
    private Date releaseDate;
    private double popularity;
    private int voteAverage;

    private Integer runtime;
    private String imdbID;

    public Movie(JSONObject obj) throws JSONException {
        this.id = obj.getInt("id");
        this.posterURL = obj.getString("poster_path");
        this.title = obj.getString("title");
        this.genreIds = getGenreIDs(obj);
        this.description = obj.getString("overview");
        this.releaseDate = DateUtils.getDateFromString(obj.getString("release_date"));
        this.popularity = obj.getDouble("popularity");
        this.voteAverage = (int) Math.round(obj.getDouble("vote_average"));

        if (obj.has("runtime")) {
            this.runtime = obj.getInt("runtime");
        }

        if (obj.has("imdb_id")) {
            this.imdbID = obj.getString("imdb_id");
        }
    }

    protected Movie(Parcel in) {
        id = in.readInt();
        posterURL = in.readString();
        title = in.readString();
        genreIds = in.createIntArray();
        description = in.readString();

        long inReleaseDate = in.readLong();
        if (inReleaseDate == -1) {
            releaseDate = null;
        } else {
            releaseDate = new Date(inReleaseDate);
        }

        popularity = in.readDouble();
        voteAverage = in.readInt();

        int inRuntime = in.readInt();
        runtime = (inRuntime != -1) ? inRuntime : null;

        imdbID = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    private static int[] getGenreIDs(JSONObject obj) throws JSONException {
        int[] genreIds;

        if (obj.has("genre_ids")) {
            genreIds = buildGenreIdsArray(obj.getJSONArray("genre_ids"));
        } else {
            genreIds = new int[0];
        }

        return genreIds;
    }

    private static int[] buildGenreIdsArray(JSONArray arr) throws JSONException {
        ArrayList<Integer> genreIds = new ArrayList<>();
        final int length = arr.length();

        for (int i = 0; i < length; i++) {
            genreIds.add(arr.getInt(i));
        }

        int[] genreIdsArr = new int[genreIds.size()];
        for (int i = 0; i < genreIds.size(); i++) {
            genreIdsArr[i] = genreIds.get(i);
        }

        return genreIdsArr;
    }

    public int[] getGenreIDs() {
        return this.genreIds;
    }

    public String getPrettyGenres(Context context) {
        String[] genres = new String[this.genreIds.length];

        for (int i = 0; i < genres.length; i++) {
            genres[i] = GenreUtils.getGenreName(context, this.genreIds[i]);
        }

        Arrays.sort(genres);

        StringBuilder result = new StringBuilder(genres[0]);
        for (int i = 1; i < genres.length; i++) {
            result.append(", ").append(genres[i]);
        }
        return result.toString();
    }

    public Integer getID() {
        return this.id;
    }

    public String getPosterURL() {
        return this.posterURL;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    private int getVoteAverage() {
        return voteAverage;
    }

    public String getPrettyVoteAverage() {
        return this.voteAverage + " / 10";
    }

    public Integer getRuntime() {
        return this.runtime;
    }

    public String getPrettyRuntime() {
        final String hours = String.format(Locale.getDefault(), "%01d", this.runtime / 60);
        final String minutes = String.format(Locale.getDefault(), "%02d", this.runtime % 60);
        return String.format("%s:%s", hours, minutes);
    }

    public String getIMDbID() {
        return this.imdbID;
    }

    public Date getReleaseDate() {
        return this.releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public boolean isUnreleased() {
        Date today = DateUtils.getMidnightCalendar().getTime();
        return this.getReleaseDate() != null && this.getReleaseDate().after(today);
    }

    public String getReleaseYear(Context context) {
        if (releaseDate == null) {
            return context.getString(R.string.no_information);
        }

        Calendar release = Calendar.getInstance();
        release.setTime(releaseDate);

        Calendar now = Calendar.getInstance();

        if (release.after(now)) {
            return DateUtils.getDateInLocalFormat(release);
        } else {
            return Integer.toString(release.get(Calendar.YEAR));
        }
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public void setIMDbID(String imdbID) {
        this.imdbID = imdbID;
    }

    public boolean hasAdditionalInformation() {
        try {
            Integer runtime = this.getRuntime();
            String imdbID = this.getIMDbID();

            return runtime != null && imdbID != null;
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Movie movie = (Movie) o;
        return id == movie.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public int compareTo(@NonNull Movie other) {
        if (this.popularity < other.popularity) {
            return 1;
        } else if (this.popularity == other.popularity) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(posterURL);
        dest.writeString(title);
        dest.writeIntArray(genreIds);
        dest.writeString(description);

        if (releaseDate == null) {
            dest.writeLong(-1);
        } else {
            dest.writeLong(releaseDate.getTime());
        }

        dest.writeDouble(popularity);
        dest.writeInt(voteAverage);
        dest.writeInt(runtime != null ? runtime : -1);
        dest.writeString(imdbID);
    }
}
