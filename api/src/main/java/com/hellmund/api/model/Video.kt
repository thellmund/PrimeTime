package com.hellmund.api.model

data class VideosResponse(val results: List<Video>)

data class Video(
    val site: String,
    val type: String,
    val key: String
)
