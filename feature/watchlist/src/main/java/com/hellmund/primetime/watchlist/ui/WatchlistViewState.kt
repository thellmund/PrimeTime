package com.hellmund.primetime.watchlist.ui

data class WatchlistViewState(
    val data: List<WatchlistMovieViewEntity> = emptyList(),
    val error: Throwable? = null,
    val deletedIndex: Int? = null,
    val showHistoryButton: Boolean = false
) {

    fun toData(
        data: List<WatchlistMovieViewEntity>
    ) = copy(data = data, error = null, deletedIndex = null)

    fun remove(
        removedItem: WatchlistMovieViewEntity
    ): WatchlistViewState {
        val index = data.indexOf(removedItem)
        return copy(data = data.minus(removedItem), deletedIndex = index)
    }

    fun toError(t: Throwable) = copy(error = t, deletedIndex = null)
}
