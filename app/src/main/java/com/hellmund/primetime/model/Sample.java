package com.hellmund.primetime.model;

import android.support.annotation.NonNull;

import com.hellmund.primetime.utils.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Sample implements Comparable<Sample> {

    private int id;
    private boolean selected;
    private String title;
    private String poster;
    private double popularity;
    private Date releaseDate;

    public int getID() {
        return id;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void toggleSelection() {
        this.selected = !(this.selected);
    }

    public String getTitle() {
        return title;
    }

    public String getPoster() {
        return poster;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    @Override
    public int compareTo(@NonNull Sample o) {
        if (this.popularity < o.popularity) {
            return 1;
        } else if (this.popularity == o.popularity) {
            return 0;
        } else {
            return -1;
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

        Sample sample = (Sample) o;
        return id == sample.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public Sample(JSONObject obj) throws JSONException {
        this.id = obj.getInt("id");
        this.title = obj.getString("title");
        this.poster = obj.getString("poster_path");
        this.popularity = obj.getDouble("popularity");
        this.releaseDate = DateUtils.getDateFromString(obj.getString("release_date"));
        this.selected = false;
    }
}
