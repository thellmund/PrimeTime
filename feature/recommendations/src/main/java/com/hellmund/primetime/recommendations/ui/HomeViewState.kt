package com.hellmund.primetime.recommendations.ui

import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.ui_common.MovieViewEntity

data class HomeViewState(
    val recommendationsType: RecommendationsType = RecommendationsType.Personalized(),
    val data: List<MovieViewEntity.Partial> = emptyList(),
    val filtered: List<MovieViewEntity.Partial>? = null,
    val pagesLoaded: Int = 0,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: Throwable? = null,
    val showFilterButton: Boolean = false,
    val showPersonalizationBanner: Boolean = false,
    val preferredGenres: List<Genre> = emptyList()
)
