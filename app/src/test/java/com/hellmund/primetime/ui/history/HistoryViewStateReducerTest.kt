package com.hellmund.primetime.ui.history

import com.hellmund.primetime.data.model.HistoryMovie
import com.hellmund.primetime.data.model.Rating
import com.hellmund.primetime.history.ui.HistoryMovieViewEntity
import com.hellmund.primetime.history.ui.HistoryViewState
import com.hellmund.primetime.history.ui.HistoryViewStateReducer
import com.hellmund.primetime.history.ui.ViewResult
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.mock

class HistoryViewStateReducerTest {

    @Test
    fun `correct state when result is data`() {
        // Given
        val state = HistoryViewState()
        val underTest = HistoryViewStateReducer()

        // When
        val result = ViewResult.Data(emptyList())
        val newState = underTest(state, result)

        // Then
        assertEquals(newState.data, result.data)
        assertFalse(newState.isLoading)
        assertNull(newState.error)
    }

    @Test
    fun `correct state when result is error`() {
        // Given
        val state = HistoryViewState()
        val underTest = HistoryViewStateReducer()

        // When
        val result = ViewResult.Error(IOException())
        val newState = underTest(state, result)

        // Then
        assertEquals(newState.data, state.data)
        assertFalse(newState.isLoading)
        assertNotNull(newState.error)
    }

    @Test
    fun `correct state when removing item`() {
        // Given
        val state = HistoryViewState(data = MOVIES)
        val underTest = HistoryViewStateReducer()

        // When
        val result = ViewResult.Removed(MOVIES.first())
        val newState = underTest(state, result)

        // Then
        assertEquals(newState.data, MOVIES.subList(1, MOVIES.size))
        assertFalse(newState.isLoading)
        assertNull(newState.error)
    }

    private companion object {
        private val MOCK_MOVIE = mock(HistoryMovie::class.java)
        private val MOVIES = listOf(
            HistoryMovieViewEntity(1, "Title 1", Rating.Like, "Time", "Details", MOCK_MOVIE),
            HistoryMovieViewEntity(2, "Title 2", Rating.Dislike, "Time", "Details", MOCK_MOVIE),
            HistoryMovieViewEntity(3, "Title 3", Rating.Like, "Time", "Details", MOCK_MOVIE)
        )
    }
}
