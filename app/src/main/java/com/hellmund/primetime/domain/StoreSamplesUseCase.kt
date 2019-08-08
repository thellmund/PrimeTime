package com.hellmund.primetime.domain

import com.hellmund.primetime.ui.selectmovies.Sample
import com.hellmund.primetime.ui.selectmovies.SamplesRepository
import javax.inject.Inject

class StoreSamplesUseCase @Inject constructor(
    private val samplesRepository: SamplesRepository
) {

    suspend operator fun invoke(samples: List<Sample>) {
        val historyMovies = samples.map { it.toHistoryMovie() }
        samplesRepository.store(historyMovies)
    }

}
