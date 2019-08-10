package com.hellmund.primetime.ui.shared

import com.hellmund.primetime.ui.suggestions.RecommendationsType
import com.hellmund.primetime.ui_common.SingleLiveDataEvent

class NavigationEventsStore : ViewEventsStore<NavigationEvent>()

class NavigationEvent(
    value: RecommendationsType
) : SingleLiveDataEvent<RecommendationsType>(value)
