package com.hellmund.primetime.model.realm;


import java.util.Date;

import io.realm.RealmObject;

public class RealmWatchlistMovie extends RealmObject {

    private int id = 0;
    private String posterUrl = null;
    private String title = null;
    private int runtime = 0;
    private Date releaseDate;
    private long timestamp = 0;
    private boolean notificationsActivated;

    public int getId() {
        return id;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getTitle() {
        return title;
    }

    public int getRuntime() {
        return runtime;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean getNotificationsActivated() {
        return notificationsActivated;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPosterUrl(String posterURL) {
        this.posterUrl = posterURL;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setNotificationsActivated(boolean notificationsActivated) {
        this.notificationsActivated = notificationsActivated;
    }
}
