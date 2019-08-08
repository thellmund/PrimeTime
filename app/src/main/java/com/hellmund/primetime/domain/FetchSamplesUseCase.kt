package com.hellmund.primetime.domain

import com.hellmund.primetime.ui.selectgenres.GenresRepository
import com.hellmund.primetime.ui.selectmovies.Sample
import com.hellmund.primetime.ui.selectmovies.SamplesRepository
import javax.inject.Inject

class FetchSamplesUseCase @Inject constructor(
    private val genresRepository: GenresRepository,
    private val samplesRepository: SamplesRepository
) {

    suspend operator fun invoke(page: Int): List<Sample> {
        val genres = genresRepository.getPreferredGenres()
        return samplesRepository.fetch(genres, page)
    }

}
