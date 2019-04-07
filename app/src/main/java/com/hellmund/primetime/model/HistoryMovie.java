package com.hellmund.primetime.model;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.hellmund.primetime.R;
import com.hellmund.primetime.utils.Constants;
import com.hellmund.primetime.utils.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class HistoryMovie implements Parcelable {

    private int id;
    private String title;
    private int rating;
    private long timestamp;
    private boolean isUpdating;

    /*public HistoryMovie(RealmHistoryMovie realmMovie) {
        this.id = realmMovie.getId();
        this.title = realmMovie.getTitle();
        this.rating = realmMovie.getRating();
        this.timestamp = realmMovie.getTimestamp();
        this.isUpdating = false;
    }*/

    private HistoryMovie(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.rating = in.readInt();
        this.timestamp = in.readLong();
        this.isUpdating = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeInt(rating);
        dest.writeLong(timestamp);
        dest.writeByte((byte) (isUpdating ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<HistoryMovie> CREATOR = new Creator<HistoryMovie>() {
        @Override
        public HistoryMovie createFromParcel(Parcel in) {
            return new HistoryMovie(in);
        }

        @Override
        public HistoryMovie[] newArray(int size) {
            return new HistoryMovie[size];
        }
    };

    public static HistoryMovie fromJSON(JSONObject movie) {
        try {
            return new HistoryMovie(
                    movie.getInt("id"),
                    movie.getString("title"),
                    movie.getInt("rating"),
                    DateUtils.getEpochFromString(movie.getString("added_at"))
            );
        } catch (JSONException e) {
            return null;
        }
    }

    public Integer getID() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public Integer getRating() {
        return this.rating;
    }

    public boolean isUpdating() {
        return this.isUpdating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setUpdating(boolean isUpdating) {
        this.isUpdating = isUpdating;
    }

    public String getSubhead(Context context) {
        final String rating = this.getPrettyRating(context);
        final String timestamp = this.getPrettyTimestamp();
        return String.format(context.getString(R.string.added_on), rating, timestamp);
    }

    private String getPrettyRating(Context context) {
        final int resId = (rating == Constants.LIKE) ? R.string.liked : R.string.disliked;
        return context.getString(resId);
    }

    private String getPrettyTimestamp() {
        if (timestamp == -1) {
            return null;
        }

        return DateUtils.getDateInLocalFormat(timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HistoryMovie that = (HistoryMovie) o;
        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    private HistoryMovie(int id, String title, int rating, long timestamp) {
        this.id = id;
        this.title = title;
        this.rating = rating;
        this.timestamp = timestamp;
        this.isUpdating = false;
    }

}
