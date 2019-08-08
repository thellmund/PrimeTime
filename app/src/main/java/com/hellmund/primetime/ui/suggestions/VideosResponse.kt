package com.hellmund.primetime.ui.suggestions

data class VideosResponse(val results: List<Video>)

data class Video(
    val site: String,
    val type: String,
    val key: String
)

object VideoResolver {

    fun findBest(title: String, videos: List<Video>): String {
        val youTubeVideos = videos.filter { it.site == "YouTube" }
        return if (youTubeVideos.isNotEmpty()) {
            youTubeVideos
                .map { it.key }
                .map { "https://www.youtube.com/watch?v=$it" }
                .first()
        } else {
            createYouTubeQueryUrl(title)
        }
    }

    private fun createYouTubeQueryUrl(title: String): String {
        val query = "$title Trailer"
        return "http://www.youtube.com/results?search_query=$query"
    }

}
