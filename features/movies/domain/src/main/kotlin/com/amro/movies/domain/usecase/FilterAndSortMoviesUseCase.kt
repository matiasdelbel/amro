package com.amro.movies.domain.usecase

import com.amro.movies.domain.Movie
import javax.inject.Inject

enum class SortCriterion { Popularity, Title, ReleaseDate }
enum class SortDirection { Ascending, Descending }

data class SortOption(
    val criterion: SortCriterion = SortCriterion.Popularity,
    val direction: SortDirection = SortDirection.Descending,
) {
    companion object {
        /** Popularity descending, as required by the MVP spec. */
        val Default = SortOption(SortCriterion.Popularity, SortDirection.Descending)
    }
}

/**
 * Pure, side-effect-free filtering + sorting for trending movies.
 *
 * Design choices:
 * - Genre filter is inclusive-OR: a movie matches if any of its genre ids is in [genreFilter].
 *   An empty filter means "show all" (no filtering), matching the spec's "top 100" behaviour.
 * - Sort is stable. `null` release dates sort **last** regardless of direction, so users never
 *   see a block of "unknown date" items at the top when sorting ascending.
 * - The use case has no dependency on Android, coroutines, or the repository, so it's trivial
 *   to unit-test — which is exactly what protects the "filter within the 100 we already have"
 *   rule from regressing.
 */
class FilterAndSortMoviesUseCase @Inject constructor() {

    operator fun invoke(
        movies: List<Movie>,
        genreFilter: Set<Int> = emptySet(),
        sort: SortOption = SortOption.Default,
    ): List<Movie> {
        val filtered = if (genreFilter.isEmpty()) {
            movies
        } else {
            movies.filter { movie -> movie.genreIds.any { it in genreFilter } }
        }

        val ascending = sort.direction == SortDirection.Ascending
        return when (sort.criterion) {
            SortCriterion.Popularity ->
                if (ascending) filtered.sortedBy { it.popularity }
                else filtered.sortedByDescending { it.popularity }

            SortCriterion.Title ->
                if (ascending) filtered.sortedBy { it.title.lowercase() }
                else filtered.sortedByDescending { it.title.lowercase() }

            SortCriterion.ReleaseDate -> {
                val withDates = filtered.filter { it.releaseDate != null }
                val withoutDates = filtered.filter { it.releaseDate == null }
                val sorted = if (ascending) {
                    withDates.sortedBy { it.releaseDate }
                } else {
                    withDates.sortedByDescending { it.releaseDate }
                }
                sorted + withoutDates
            }
        }
    }
}
