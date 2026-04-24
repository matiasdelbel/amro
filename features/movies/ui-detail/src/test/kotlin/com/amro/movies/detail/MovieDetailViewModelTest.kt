package com.amro.movies.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.amro.core.common.result.DomainError
import com.amro.core.common.result.DomainResult
import com.amro.movies.domain.Genre
import com.amro.movies.domain.MovieDetail
import com.amro.movies.domain.MovieStatus
import com.amro.movies.domain.repository.MoviesRepository
import com.amro.movies.domain.usecase.GetMovieDetailUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest {

    private val repository: MoviesRepository = mockk()

    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `reads movieId from SavedStateHandle and loads detail`() = runTest {
        val detail = MovieDetail(
            id = 42,
            title = "Life",
            tagline = null,
            posterUrl = null,
            backdropUrl = null,
            genres = listOf(Genre(1, "Drama")),
            overview = "A movie.",
            voteAverage = 8.0,
            voteCount = 100,
            budget = 0,
            revenue = 0,
            status = MovieStatus.Released,
            imdbUrl = null,
            runtimeMinutes = 120,
            releaseDate = null,
            homepage = null,
        )
        coEvery { repository.getMovieDetail(42) } returns DomainResult.Success(detail)

        val vm = build(movieId = 42)

        vm.state.test {
            // Skip Loading
            var state = awaitItem()
            if (state is MovieDetailUiState.Loading) state = awaitItem()
            assertThat(state).isInstanceOf(MovieDetailUiState.Content::class.java)
            assertThat((state as MovieDetailUiState.Content).movie.id).isEqualTo(42)
        }
    }

    @Test
    fun `server failure is surfaced as Error state carrying the typed DomainError`() = runTest {
        coEvery { repository.getMovieDetail(7) } returns DomainResult.Failure(DomainError.Server(404))
        val vm = build(movieId = 7)

        vm.state.test {
            var state = awaitItem()
            if (state is MovieDetailUiState.Loading) state = awaitItem()
            assertThat(state).isInstanceOf(MovieDetailUiState.Error::class.java)
            // The VM no longer stringifies the error — it just propagates the typed
            // `DomainError` so the screen can localise it via `stringResource(...)`.
            val error = (state as MovieDetailUiState.Error).error
            assertThat(error).isInstanceOf(DomainError.Server::class.java)
            assertThat((error as DomainError.Server).code).isEqualTo(404)
        }
    }

    private fun build(movieId: Long) = MovieDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf(MOVIE_ID_ARG to movieId)),
        getMovieDetail = GetMovieDetailUseCase(repository),
    )
}
