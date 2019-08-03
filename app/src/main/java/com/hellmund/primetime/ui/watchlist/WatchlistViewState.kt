package com.hellmund.primetime.ui.watchlist

data class WatchlistViewState(
    val data: List<WatchlistMovieViewEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: Throwable? = null,
    val deletedIndex: Int? = null
) {

    fun toData(
        data: List<WatchlistMovieViewEntity>
    ) = copy(data = data, isLoading = false, error = null, deletedIndex = null)

    fun remove(
        removedItem: WatchlistMovieViewEntity
    ): WatchlistViewState {
        val index = data.indexOf(removedItem)
        return copy(data = data.minus(removedItem), deletedIndex = index)
    }

    fun toError(t: Throwable) = copy(isLoading = false, error = t, deletedIndex = null)

}
