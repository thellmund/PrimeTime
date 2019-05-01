package com.hellmund.primetime.ui.search;

import android.os.Parcel;
import android.os.Parcelable;

import com.hellmund.primetime.data.model.Movie;

import org.threeten.bp.LocalDate;

@Deprecated
public class SearchResult implements Parcelable {

    private int id;
    private String posterPath;
    private String title;
    private String description;
    private LocalDate releaseDate;
    private int runtime;

    private SearchResult(Parcel in) {
        id = in.readInt();
        posterPath = in.readString();
        title = in.readString();
        description = in.readString();
        releaseDate = (LocalDate) in.readSerializable();
        runtime = in.readInt();
    }

    public static final Creator<SearchResult> CREATOR = new Creator<SearchResult>() {
        @Override
        public SearchResult createFromParcel(Parcel in) {
            return new SearchResult(in);
        }

        @Override
        public SearchResult[] newArray(int size) {
            return new SearchResult[size];
        }
    };

    public int getID() {
        return this.id;
    }

    public String getFullPosterPath() {
        return "http://image.tmdb.org/t/p/w780" + posterPath;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public SearchResult(int id, String path, String title, String description, LocalDate releaseDate) {
        this.id = id;
        this.posterPath = path;
        this.title = title;
        this.description = description;
        this.releaseDate = releaseDate;
    }

    public static SearchResult fromMovie(Movie movie) {
        return new SearchResult(movie.getId(), movie.getPosterPath(), movie.getTitle(),
                movie.getDescription(), movie.getReleaseDate());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(posterPath);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeSerializable(releaseDate);
        dest.writeInt(runtime);
    }
}
