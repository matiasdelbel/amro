package com.amro.movies.domain.usecase

import com.amro.core.domain.DomainError
import com.amro.core.domain.DomainResult
import com.amro.movies.domain.MovieDetail
import com.amro.movies.domain.MovieStatus
import com.amro.movies.domain.repository.MoviesRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * The use case is a one-line pass-through over [MoviesRepository.getMovieDetail], so the
 * coverage focuses on what would actually break consumers: that the `movieId` argument is
 * forwarded verbatim to the repository, and that both `Success` and `Failure` outcomes are
 * returned untouched (no swallowing, no rewrapping).
 */
class GetMovieDetailUseCaseTest {

    private val repository: MoviesRepository = mockk()
    private val useCase = GetMovieDetailUseCase(repository)

    @Test
    fun `forwards movieId to repository and returns Success payload`() = runTest {
        val movieId = 27_205L
        val detail = movieDetail(id = movieId, title = "Inception")
        coEvery { repository.getMovieDetail(movieId) } returns DomainResult.Success(detail)

        val result = useCase(movieId)

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        assertThat((result as DomainResult.Success).value).isSameInstanceAs(detail)
        coVerify(exactly = 1) { repository.getMovieDetail(movieId) }
        confirmVerified(repository)
    }

    @Test
    fun `forwards Failure from repository unchanged`() = runTest {
        val failure = DomainResult.Failure(DomainError.Server(code = 404))
        coEvery { repository.getMovieDetail(any()) } returns failure

        val result = useCase(movieId = 7L)

        assertThat(result).isSameInstanceAs(failure)
        coVerify(exactly = 1) { repository.getMovieDetail(7L) }
        confirmVerified(repository)
    }

    private fun movieDetail(id: Long, title: String) = MovieDetail(
        id = id,
        title = title,
        tagline = null,
        posterUrl = null,
        backdropUrl = null,
        genres = emptyList(),
        overview = null,
        voteAverage = 0.0,
        voteCount = 0,
        budget = 0,
        revenue = 0,
        status = MovieStatus.Released,
        imdbUrl = null,
        runtimeMinutes = null,
        releaseDate = null,
        homepage = null,
    )
}
