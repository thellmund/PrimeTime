package com.hellmund.primetime.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hellmund.api.model.ApiGenre
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "genres")
data class Genre(
    @PrimaryKey var id: Int,
    var name: String,
    var isPreferred: Boolean = false,
    var isExcluded: Boolean = false
) : Parcelable {

    companion object {
        fun from(apiGenre: ApiGenre) = Genre(apiGenre.id, apiGenre.name)
    }

}
