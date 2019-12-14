package com.hellmund.primetime.recommendations.ui

import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.repositories.GenresRepository

val GENRES = listOf(
    Genre.Impl(id = 1L, name = "1", isPreferred = true, isExcluded = false),
    Genre.Impl(id = 2L, name = "2", isPreferred = true, isExcluded = false),
    Genre.Impl(id = 3L, name = "3", isPreferred = false, isExcluded = true)
)

class FakeGenresRepository : GenresRepository {

    override suspend fun getAll(): List<Genre> = GENRES

    override suspend fun getPreferredGenres(): List<Genre> = GENRES.filter { it.isPreferred }

    override suspend fun getExcludedGenres(): List<Genre> = GENRES.filter { it.isPreferred }

    override suspend fun fetchGenres(): List<Genre> = GENRES

    override suspend fun getGenreById(genreId: Long): Genre = GENRES[0]

    override suspend fun getGenreByName(name: String): Genre = GENRES[0]

    override suspend fun getGenres(genreIds: Set<String>): List<Genre> = GENRES

    override suspend fun storeGenres(genres: List<Genre>) = Unit
}
