package com.hellmund.primetime.ui.selectgenres

import com.hellmund.primetime.data.model.Genre
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class GenresViewStateReducerTest {

    @Test
    fun `correct state when loading`() {
        // Given
        val state = SelectGenresViewState()
        val underTest = GenresViewStateReducer()

        // When
        val result = Result.Loading
        val newState = underTest(state, result)

        // Then
        assertTrue(newState.isLoading)
        assertTrue(newState.data.isEmpty())
        assertNull(newState.error)
    }

    @Test
    fun `correct state when result is data`() {
        // Given
        val state = SelectGenresViewState()
        val underTest = GenresViewStateReducer()

        // When
        val result = Result.Data(GENRES)
        val newState = underTest(state, result)

        // Then
        assertFalse(newState.isLoading)
        assertEquals(newState.data, GENRES)
        assertNull(newState.error)
    }

    @Test
    fun `correct state when result is error`() {
        // Given
        val state = SelectGenresViewState()
        val underTest = GenresViewStateReducer()

        // When
        val result = Result.Error(IOException())
        val newState = underTest(state, result)

        // Then
        assertFalse(newState.isLoading)
        assertTrue(newState.data.isEmpty())
        assertNotNull(newState.error)
    }

    @Test
    fun `correct state when genre is toggled`() {
        // Given
        val state = SelectGenresViewState(data = GENRES)
        val underTest = GenresViewStateReducer()

        // When
        val updatedGenre = GENRES.first().copy(isPreferred = true)
        val result = Result.GenreToggled(updatedGenre)
        val newState = underTest(state, result)

        // Then
        val expected = listOf(updatedGenre) + GENRES.subList(1, GENRES.size)
        assertFalse(newState.isLoading)
        assertEquals(newState.data, expected)
        assertNull(newState.error)
    }

    private companion object {
        private val GENRES = listOf(Genre(1, "Genre 1"), Genre(2, "Genre 2"), Genre(3, "Genre 3"))
    }

}