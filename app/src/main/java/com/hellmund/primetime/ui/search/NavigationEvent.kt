package com.hellmund.primetime.ui.search

import com.hellmund.primetime.ui.suggestions.RecommendationsType

class NavigationEvent(
    private val recommendationsType: RecommendationsType
) {

    var hasBeenHandled = false
        private set

    fun getIfNotHandled(): RecommendationsType? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            recommendationsType
        }
    }

}
