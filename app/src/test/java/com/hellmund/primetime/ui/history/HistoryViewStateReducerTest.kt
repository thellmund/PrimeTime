package com.hellmund.primetime.ui.history

import com.hellmund.primetime.data.model.HistoryMovie
import com.hellmund.primetime.data.model.Rating
import com.hellmund.primetime.history.HistoryViewState
import com.hellmund.primetime.history.HistoryViewStateReducer
import com.hellmund.primetime.history.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.mock
import java.io.IOException

class HistoryViewStateReducerTest {

    @Test
    fun `correct state when result is data`() {
        // Given
        val state = HistoryViewState()
        val underTest = HistoryViewStateReducer()

        // When
        val result = Result.Data(emptyList())
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
        val result = Result.Error(IOException())
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
        val result = Result.Removed(MOVIES.first())
        val newState = underTest(state, result)

        // Then
        assertEquals(newState.data, MOVIES.subList(1, MOVIES.size))
        assertFalse(newState.isLoading)
        assertNull(newState.error)
    }

    @Test
    fun `correct state when updating item`() {
        // Given
        val state = HistoryViewState(data = MOVIES)
        val underTest = HistoryViewStateReducer()

        // When
        val updatedMovie = MOVIES.first().copy(rating = Rating.Like)
        val result = Result.Updated(updatedMovie)
        val newState = underTest(state, result)

        // Then
        val expected = listOf(updatedMovie) + MOVIES.subList(1, MOVIES.size)
        assertEquals(newState.data, expected)
        assertFalse(newState.isLoading)
        assertNull(newState.error)
    }

    private companion object {
        private val MOCK_MOVIE = mock(HistoryMovie::class.java)
        private val MOVIES = listOf(
            com.hellmund.primetime.history.HistoryMovieViewEntity(1, "Title 1", Rating.Like, "Time", "Details", MOCK_MOVIE),
            com.hellmund.primetime.history.HistoryMovieViewEntity(2, "Title 2", Rating.Dislike, "Time", "Details", MOCK_MOVIE),
            com.hellmund.primetime.history.HistoryMovieViewEntity(3, "Title 3", Rating.Like, "Time", "Details", MOCK_MOVIE)
        )
    }

}
