package com.hellmund.primetime.ui.watchlist

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDateTime

@Parcelize
data class WatchlistMovieViewEntity(
        var id: Int,
        var title: String,
        var posterUrl: String,
        var formattedRuntime: String,
        var formattedReleaseDate: String,
        var savedAt: LocalDateTime = LocalDateTime.now(),
        var notificationsActivated: Boolean = true
) : Parcelable
