package com.hellmund.primetime.ui.suggestions.details

import com.google.gson.annotations.SerializedName

data class ReviewsResponse(
        @SerializedName("results") val results: List<Review>,
        @SerializedName("total_pages") val totalPages: Int,
        @SerializedName("page") val page: Int
)

data class Review(
        val author: String,
        val content: String
)
