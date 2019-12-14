package com.hellmund.primetime.recommendations.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.data.repositories.HistoryRepository
import com.hellmund.primetime.recommendations.data.MovieRankingProcessor
import com.hellmund.primetime.ui_common.MovieViewEntitiesMapper
import com.hellmund.primetime.ui_common.RealMovieViewEntitiesMapper
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class HomeViewModelTest {

    private val moviesRepository = FakeMoviesRepository()
    private val genresRepository = FakeGenresRepository()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Test
    fun foo() = runBlocking {
        val underTest = createHomeViewModel(RecommendationsType.Personalized())
        val viewState = underTest.viewState

        val mapper = RealMovieViewEntitiesMapper(FakeValueFormatter(), genresRepository)
        val viewEntities = mapper.mapPartialMovies(FAKE_PARTIAL_MOVIES)

        viewState.assertEmissions(
            HomeViewState(isLoading = true),
            HomeViewState(isLoading = false, data = viewEntities)
        )
    }

    private fun createHomeViewModel(
        recommendationsType: RecommendationsType
    ) = HomeViewModel(
        repository = moviesRepository,
        historyRepository = mock(HistoryRepository::class.java),
        rankingProcessor = mock(MovieRankingProcessor::class.java),
        viewEntitiesMapper = mock(MovieViewEntitiesMapper::class.java),
        recommendationsType = recommendationsType
    )
}

fun <T> LiveData<T>.assertEmissions(vararg emissions: T) {
    observeForTesting {
        for (emission in emissions) {
            assert(value == emission)
        }
    }
}

fun <T> LiveData<T>.observeForTesting(block: LiveData<T>.() -> Unit) {
    val observer = Observer<T> { }
    try {
        observeForever(observer)
        this.block()
    } finally {
        removeObserver(observer)
    }
}
