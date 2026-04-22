package com.amro.movies.listing

import com.amro.movies.domain.model.Genre
import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.usecase.SortOption

/**
 * Single immutable UI state for the trending list screen.
 *
 * We keep both [allMovies] (the unfiltered 100 the backend returned) and [visibleMovies] (the
 * result of applying filter + sort) so the filter/sort controls can recompute instantly without
 * needing a new network call — matching the spec rule "filter within the shown top 100".
 */
data class MoviesListingUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val allMovies: List<Movie> = emptyList(),
    val visibleMovies: List<Movie> = emptyList(),
    val availableGenres: List<Genre> = emptyList(),
    val selectedGenreIds: Set<Int> = emptySet(),
    val sortOption: SortOption = SortOption.Default,
    val errorMessage: String? = null,
) {
    val hasContent: Boolean get() = allMovies.isNotEmpty()
}
