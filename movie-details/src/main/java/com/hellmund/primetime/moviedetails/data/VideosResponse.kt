package com.hellmund.primetime.moviedetails.data

import com.hellmund.api.model.Video

object VideoResolver {

    fun findBest(title: String, videos: List<Video>): String {
        val youTubeVideos = videos.filter { it.site == "YouTube" }
        return if (youTubeVideos.isNotEmpty()) {
            youTubeVideos
                .map { it.key }
                .map { "https://www.youtube.com/watch?v=$it" }
                .first()
        } else {
            buildYouTubeUrl(title)
        }
    }

    private fun buildYouTubeUrl(title: String): String {
        val query = "$title Trailer"
        return "http://www.youtube.com/results?search_query=$query"
    }

}
