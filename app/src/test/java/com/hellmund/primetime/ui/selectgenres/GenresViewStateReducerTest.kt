package com.hellmund.primetime.ui.selectgenres

import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.onboarding.ui.selectgenres.GenresViewStateReducer
import com.hellmund.primetime.onboarding.ui.selectgenres.SelectGenresViewState
import com.hellmund.primetime.onboarding.ui.selectgenres.ViewResult
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GenresViewStateReducerTest {

    @Test
    fun `correct state when loading`() {
        // Given
        val state = SelectGenresViewState()
        val underTest = GenresViewStateReducer()

        // When
        val result = ViewResult.Loading
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
        val result = ViewResult.Data(GENRES)
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
        val result = ViewResult.Error(IOException())
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
        val result = ViewResult.GenreToggled(updatedGenre)
        val newState = underTest(state, result)

        // Then
        val expected = listOf(updatedGenre) + GENRES.subList(1, GENRES.size)
        assertFalse(newState.isLoading)
        assertEquals(newState.data, expected)
        assertNull(newState.error)
    }

    private companion object {
        private val GENRES = listOf(
            Genre.Impl(id = 1, name = "Genre 1", isPreferred = false, isExcluded = false),
            Genre.Impl(id = 2, name = "Genre 2", isPreferred = false, isExcluded = false),
            Genre.Impl(id = 3, name = "Genre 3", isPreferred = false, isExcluded = false)
        )
    }
}
