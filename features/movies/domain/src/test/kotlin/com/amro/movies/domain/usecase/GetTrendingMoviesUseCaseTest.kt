package com.amro.movies.domain.usecase

import com.amro.core.domain.DomainError
import com.amro.core.domain.DomainResult
import com.amro.movies.domain.Genre
import com.amro.movies.domain.Movie
import com.amro.movies.domain.repository.MoviesRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * The use case is a one-line pass-through over [MoviesRepository.getTrendingTop100], so the
 * coverage focuses on what would actually break consumers: that we hit the right repository
 * method and that we forward both `Success` and `Failure` outcomes verbatim — no swallowing,
 * no rewrapping.
 */
class GetTrendingMoviesUseCaseTest {

    private val repository: MoviesRepository = mockk()
    private val useCase = GetTrendingMoviesUseCase(repository)

    @Test
    fun `forwards Success payload from repository unchanged`() = runTest {
        val movies = listOf(
            movie(id = 1, title = "Alpha"),
            movie(id = 2, title = "Bravo"),
        )
        coEvery { repository.getTrendingTop100() } returns DomainResult.Success(movies)

        val result = useCase()

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        assertThat((result as DomainResult.Success).value).isSameInstanceAs(movies)
        coVerify(exactly = 1) { repository.getTrendingTop100() }
        confirmVerified(repository)
    }

    @Test
    fun `forwards Failure from repository unchanged`() = runTest {
        val failure = DomainResult.Failure(DomainError.Network())
        coEvery { repository.getTrendingTop100() } returns failure

        val result = useCase()

        assertThat(result).isSameInstanceAs(failure)
        coVerify(exactly = 1) { repository.getTrendingTop100() }
        confirmVerified(repository)
    }

    private fun movie(id: Long, title: String) = Movie(
        id = id,
        title = title,
        posterUrl = null,
        backdropUrl = null,
        genreIds = emptyList(),
        genres = emptyList<Genre>(),
        popularity = 0.0,
        releaseDate = LocalDate.of(2024, 1, 1),
        voteAverage = 0.0,
    )
}
