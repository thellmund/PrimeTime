package com.hellmund.primetime.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.hellmund.primetime.model2.ApiMovie;
import com.hellmund.primetime.utils.DateUtils;
import com.hellmund.primetime.utils.DownloadUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class SearchResult implements Parcelable {

    private int id;
    private String posterPath;
    private String title;
    private String description;
    private Date releaseDate;
    private int runtime;

    private SearchResult(Parcel in) {
        id = in.readInt();
        posterPath = in.readString();
        title = in.readString();
        description = in.readString();
        releaseDate = new Date(in.readLong());
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

    public static SearchResult fromJSON(JSONObject json) {
        try {
            final int id = json.getInt("id");
            final String poster = DownloadUtils.getLowResPosterURL(json.getString("poster_path"));
            final String title = json.getString("title");
            final String description = json.getString("overview");
            final Date releaseDate = DateUtils.getDateFromString(json.getString("release_date"));

            return new SearchResult(id, poster, title, description, releaseDate);
        } catch (JSONException e) {
            return null;
        }
    }

    public int getID() {
        return this.id;
    }

    public String getPosterPath() {
        return this.posterPath;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public SearchResult(int id, String path, String title, String description, Date releaseDate) {
        this.id = id;
        this.posterPath = path;
        this.title = title;
        this.description = description;
        this.releaseDate = releaseDate;
    }

    public static SearchResult fromMovie(ApiMovie movie) {
        return new SearchResult(movie.getId(), movie.getPosterUrl(), movie.getTitle(),
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
        dest.writeLong(releaseDate.getTime());
        dest.writeInt(runtime);
    }
}
