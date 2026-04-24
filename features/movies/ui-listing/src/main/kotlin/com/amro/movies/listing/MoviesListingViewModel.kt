package com.amro.movies.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amro.core.common.result.DomainResult
import com.amro.movies.domain.Movie
import com.amro.movies.domain.Genre
import com.amro.movies.domain.usecase.FilterAndSortMoviesUseCase
import com.amro.movies.domain.usecase.GetTrendingMoviesUseCase
import com.amro.movies.domain.usecase.SortCriterion
import com.amro.movies.domain.usecase.SortDirection
import com.amro.movies.domain.usecase.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MoviesListingViewModel @Inject constructor(
    private val getTrendingMovies: GetTrendingMoviesUseCase,
    private val filterAndSortMovies: FilterAndSortMoviesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(value = MoviesListingUiState(isLoading = true))
    val state: StateFlow<MoviesListingUiState> = _state.asStateFlow()

    init { loadMovies(initial = true) }

    fun refresh() = loadMovies(initial = false)

    fun retry() = loadMovies(initial = true)

    fun toggleGenre(genreId: Int) {
        _state.update { current ->
            val selected = current
                .selectedGenreIds
                .toMutableSet()
                .apply { if (!add(genreId)) remove(genreId) }

            current.copy(
                selectedGenreIds = selected,
                visibleMovies = filterAndSortMovies(current.allMovies, genreFilter = selected, sort = current.sortOption),
            )
        }
    }

    fun clearGenreFilter() {
        _state.update { current ->
            current.copy(
                selectedGenreIds = emptySet(),
                visibleMovies = filterAndSortMovies(current.allMovies, emptySet(), current.sortOption),
            )
        }
    }

    fun updateSort(criterion: SortCriterion, direction: SortDirection) {
        _state.update { current ->
            val newSort = SortOption(criterion, direction)

            current.copy(
                sortOption = newSort,
                visibleMovies = filterAndSortMovies(current.allMovies, current.selectedGenreIds, newSort),
            )
        }
    }

    private fun loadMovies(initial: Boolean) {
        _state.update {
            if (initial) it.copy(isLoading = true, error = null)
            else it.copy(isRefreshing = true, error = null)
        }

        viewModelScope.launch {
            when (val result = getTrendingMovies()) {
                is DomainResult.Success -> {
                    val all = result.value
                    val genres = deriveGenres(all)
                    _state.update { current ->
                        current.copy(
                            isLoading = false,
                            isRefreshing = false,
                            allMovies = all,
                            availableGenres = genres,
                            visibleMovies = filterAndSortMovies(
                                movies = all,
                                genreFilter = current.selectedGenreIds,
                                sort = current.sortOption,
                            ),
                            error = null,
                        )
                    }
                }
                is DomainResult.Failure -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = result.error,
                        )
                    }
                }
            }
        }
    }

    /**
     * Distinct, sorted list of genres as actually present in the 100 trending movies.
     * Using only present genres makes for a short, relevant filter chip list.
     */
    private fun deriveGenres(movies: List<Movie>): List<Genre> = movies
        .asSequence()
        .flatMap { it.genres.asSequence() }
        .distinctBy { it.id }
        .sortedBy { it.name }
        .toList()
}
