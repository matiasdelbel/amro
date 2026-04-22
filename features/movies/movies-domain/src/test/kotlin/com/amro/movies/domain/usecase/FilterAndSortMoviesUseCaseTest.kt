package com.amro.movies.domain.usecase

import com.amro.movies.domain.model.Genre
import com.amro.movies.domain.model.Movie
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class FilterAndSortMoviesUseCaseTest {

    private val useCase = FilterAndSortMoviesUseCase()

    private val comedy = Genre(1, "Comedy")
    private val action = Genre(2, "Action")
    private val drama = Genre(3, "Drama")

    private val a = movie(id = 1, title = "Alpha", genres = listOf(comedy), popularity = 10.0, releaseDate = LocalDate.of(2024, 1, 1))
    private val b = movie(id = 2, title = "Bravo", genres = listOf(action), popularity = 50.0, releaseDate = LocalDate.of(2023, 6, 1))
    private val c = movie(id = 3, title = "Charlie", genres = listOf(comedy, drama), popularity = 30.0, releaseDate = null)
    private val d = movie(id = 4, title = "delta", genres = listOf(drama), popularity = 20.0, releaseDate = LocalDate.of(2022, 12, 31))

    private val all = listOf(a, b, c, d)

    @Test
    fun `empty filter returns all movies`() {
        val result = useCase(all, genreFilter = emptySet())
        assertThat(result).hasSize(4)
    }

    @Test
    fun `genre filter keeps only matching movies`() {
        val result = useCase(all, genreFilter = setOf(comedy.id))
        assertThat(result.map { it.id }).containsExactly(c.id, a.id).inOrder() // popularity desc default
    }

    @Test
    fun `multi-genre filter matches movies belonging to any selected genre`() {
        val result = useCase(all, genreFilter = setOf(action.id, drama.id))
        assertThat(result.map { it.id }).containsExactly(b.id, c.id, d.id).inOrder()
    }

    @Test
    fun `default sort is popularity descending`() {
        val result = useCase(all)
        assertThat(result.map { it.id }).containsExactly(b.id, c.id, d.id, a.id).inOrder()
    }

    @Test
    fun `title sort is case-insensitive and ascending`() {
        val result = useCase(
            all,
            sort = SortOption(SortCriterion.Title, SortDirection.Ascending),
        )
        // "Alpha" < "Bravo" < "Charlie" < "delta" (case-insensitive)
        assertThat(result.map { it.title }).containsExactly("Alpha", "Bravo", "Charlie", "delta").inOrder()
    }

    @Test
    fun `release date sort puts unknown dates last`() {
        val result = useCase(
            all,
            sort = SortOption(SortCriterion.ReleaseDate, SortDirection.Descending),
        )
        assertThat(result.first().id).isEqualTo(a.id) // 2024
        assertThat(result.last().id).isEqualTo(c.id) // null date
    }

    @Test
    fun `release date sort ascending also keeps unknown dates last`() {
        val result = useCase(
            all,
            sort = SortOption(SortCriterion.ReleaseDate, SortDirection.Ascending),
        )
        assertThat(result.first().id).isEqualTo(d.id) // 2022
        assertThat(result.last().id).isEqualTo(c.id) // null date
    }

    private fun movie(
        id: Long,
        title: String,
        genres: List<Genre>,
        popularity: Double,
        releaseDate: LocalDate?,
    ): Movie = Movie(
        id = id,
        title = title,
        posterUrl = null,
        backdropUrl = null,
        genreIds = genres.map { it.id },
        genres = genres,
        popularity = popularity,
        releaseDate = releaseDate,
        voteAverage = 7.0,
    )
}
