package com.amro.movies.listing

import app.cash.turbine.test
import com.amro.core.domain.DomainError
import com.amro.core.domain.DomainResult
import com.amro.movies.domain.Genre
import com.amro.movies.domain.Movie
import com.amro.movies.domain.repository.MoviesRepository
import com.amro.movies.domain.usecase.FilterAndSortMoviesUseCase
import com.amro.movies.domain.usecase.GetTrendingMoviesUseCase
import com.amro.movies.domain.SortCriterion
import com.amro.movies.domain.SortDirection
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import java.time.LocalDate
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
class MoviesListingViewModelTest {

    private val repository: MoviesRepository = mockk()
    private val filterAndSort = FilterAndSortMoviesUseCase()

    private val comedy = Genre(1, "Comedy")
    private val action = Genre(2, "Action")

    private val movies = listOf(
        movie(1, "Alpha", listOf(comedy), popularity = 10.0),
        movie(2, "Bravo", listOf(action), popularity = 50.0),
        movie(3, "Charlie", listOf(comedy), popularity = 30.0),
    )

    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `success state exposes all movies and derived genres`() = runTest {
        coEvery { repository.getTrendingTop100() } returns DomainResult.Success(movies)
        val vm = buildVm()

        vm.state.test {
            val final = awaitLatestStable()
            assertThat(final.isLoading).isFalse()
            assertThat(final.allMovies).hasSize(3)
            assertThat(final.visibleMovies).hasSize(3)
            assertThat(final.availableGenres.map { it.name }).containsExactly("Action", "Comedy").inOrder()
            assertThat(final.error).isNull()
        }
    }

    @Test
    fun `toggling a genre filters the visible movies but keeps allMovies intact`() = runTest {
        coEvery { repository.getTrendingTop100() } returns DomainResult.Success(movies)
        val vm = buildVm()
        // Let initial load finish before interacting.
        vm.state.test { awaitLatestStable(); cancelAndIgnoreRemainingEvents() }

        vm.toggleGenre(comedy.id)

        vm.state.test {
            val s = awaitItem()
            assertThat(s.selectedGenreIds).containsExactly(comedy.id)
            assertThat(s.visibleMovies.map { it.title }).containsExactly("Charlie", "Alpha")
            assertThat(s.allMovies).hasSize(3)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changing sort reorders visible movies`() = runTest {
        coEvery { repository.getTrendingTop100() } returns DomainResult.Success(movies)
        val vm = buildVm()
        vm.state.test { awaitLatestStable(); cancelAndIgnoreRemainingEvents() }

        vm.updateSort(SortCriterion.Title, SortDirection.Ascending)

        vm.state.test {
            val s = awaitItem()
            assertThat(s.visibleMovies.map { it.title }).containsExactly("Alpha", "Bravo", "Charlie").inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failure surfaces the typed DomainError and clears loading`() = runTest {
        coEvery { repository.getTrendingTop100() } returns DomainResult.Failure(DomainError.Network())
        val vm = buildVm()

        vm.state.test {
            val final = awaitLatestStable()
            assertThat(final.isLoading).isFalse()
            // The VM no longer stringifies the error — it just propagates the typed
            // `DomainError` so the screen can localise it via `stringResource(...)`.
            assertThat(final.error).isInstanceOf(DomainError.Network::class.java)
            assertThat(final.allMovies).isEmpty()
        }
    }

    private suspend fun app.cash.turbine.ReceiveTurbine<MoviesListingUiState>.awaitLatestStable(): MoviesListingUiState {
        var last = awaitItem()
        while (last.isLoading) last = awaitItem()
        return last
    }

    private fun buildVm() = MoviesListingViewModel(
        getTrendingMovies = GetTrendingMoviesUseCase(repository),
        filterAndSortMovies = filterAndSort,
    )

    private fun movie(id: Long, title: String, genres: List<Genre>, popularity: Double) = Movie(
        id = id,
        title = title,
        posterUrl = null,
        backdropUrl = null,
        genreIds = genres.map { it.id },
        genres = genres,
        popularity = popularity,
        releaseDate = LocalDate.of(2024, 1, 1),
        voteAverage = 7.5,
    )
}
