package com.hellmund.primetime.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.hellmund.primetime.utils.DateUtils;

import java.util.Date;

public class WatchlistMovie implements Parcelable {

    private int id;
    private String title;
    private String posterURL;
    private int runtime;
    private Date releaseDate;
    private long timestamp;
    private boolean deleted;
    private boolean notificationsActivated;

    /*public WatchlistMovie(RealmWatchlistMovie movie) {
        this.id = movie.getId();
        this.title = movie.getTitle();
        this.posterURL = movie.getPosterUrl();
        this.runtime = movie.getRuntime();
        this.releaseDate = movie.getReleaseDate();
        this.timestamp = movie.getTimestamp();
        this.notificationsActivated = movie.getNotificationsActivated();
    }*/

    private WatchlistMovie(Parcel in) {
        id = in.readInt();
        title = in.readString();
        posterURL = in.readString();
        runtime = in.readInt();
        releaseDate = new Date(in.readLong());
        timestamp = in.readLong();
        deleted = in.readByte() != 0;
        notificationsActivated = in.readByte() != 0;
    }

    public static final Creator<WatchlistMovie> CREATOR = new Creator<WatchlistMovie>() {
        @Override
        public WatchlistMovie createFromParcel(Parcel in) {
            return new WatchlistMovie(in);
        }

        @Override
        public WatchlistMovie[] newArray(int size) {
            return new WatchlistMovie[size];
        }
    };

    public Integer getID() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getPosterURL() {
        return this.posterURL;
    }

    public int getRuntime() {
        return this.runtime;
    }

    public boolean hasRuntime() {
        return this.runtime > 0;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public boolean isUnreleased() {
        Date today = DateUtils.getMidnightCalendar().getTime();
        return releaseDate != null && releaseDate.after(today);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isNotificationsActivated() {
        return notificationsActivated;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void delete() {
        this.deleted = true;
    }

    public void undelete() {
        this.deleted = false;
    }

    public void setNotificationsActivated(boolean notificationsActivated) {
        this.notificationsActivated = notificationsActivated;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(posterURL);
        dest.writeInt(runtime);
        dest.writeLong(releaseDate.getTime());
        dest.writeLong(timestamp);
        dest.writeInt(deleted ? 1 : 0);
        dest.writeInt(notificationsActivated ? 1 : 0);
    }

}
