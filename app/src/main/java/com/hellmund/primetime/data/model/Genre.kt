package com.hellmund.primetime.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "genres")
data class Genre(
    @PrimaryKey var id: Int,
    var name: String,
    var isPreferred: Boolean = false,
    var isExcluded: Boolean = false
) : Parcelable
