package com.hellmund.primetime.domain

import com.hellmund.primetime.ui.history.HistoryMovieViewEntitiesMapper
import com.hellmund.primetime.ui.history.HistoryMovieViewEntity
import com.hellmund.primetime.ui.history.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository,
    private val viewEntitiesMapper: HistoryMovieViewEntitiesMapper
) {

    suspend operator fun invoke(): Flow<List<HistoryMovieViewEntity>> {
        return repository.observeAll()
            .map { it.sortedByDescending { movie -> movie.timestamp } }
            .map { viewEntitiesMapper(it) }
    }

}
