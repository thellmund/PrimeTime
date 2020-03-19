package com.hellmund.api.model

import com.google.gson.annotations.SerializedName

data class ReviewsResponse(
    @SerializedName("results") val results: List<Review>,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("page") val page: Int
)

data class Review(
    val id: String,
    val author: String,
    val content: String
)
