package com.hellmund.primetime.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "watchlist_movies")
data class WatchlistMovie(
        @PrimaryKey var id: Int,
        var title: String,
        var posterURL: String,
        var runtime: Int,
        var releaseDate: Date,
        var timestamp: Date,
        var deleted: Boolean,
        var notificationsActivated: Boolean
): Parcelable
