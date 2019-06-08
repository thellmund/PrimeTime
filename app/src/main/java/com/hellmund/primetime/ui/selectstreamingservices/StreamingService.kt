package com.hellmund.primetime.ui.selectstreamingservices

data class StreamingService(
        val name: String,
        val isSelected: Boolean
) {

    fun toggled(): StreamingService = copy(isSelected = isSelected.not())

}
