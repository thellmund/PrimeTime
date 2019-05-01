package com.hellmund.primetime.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ApiGenre(val id: Int, val name: String) : Parcelable
