package com.amro.movies.domain.usecase

import com.amro.core.coroutine.dispatcher.DispatcherProvider
import com.amro.movies.domain.Movie
import com.amro.movies.domain.SortCriterion
import com.amro.movies.domain.SortDirection
import com.amro.movies.domain.SortOption
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

/**
 * Pure, side-effect-free filtering + sorting for trending movies.
 *
 * Design choices:
 * - Genre filter is inclusive-OR: a movie matches if any of its genre ids is in [genreFilter].
 *   An empty filter means "show all" (no filtering), matching the spec's "top 100" behaviour.
 * - Sort is stable. `null` release dates sort **last** regardless of direction, so users never
 *   see a block of "unknown date" items at the top when sorting ascending.
 * - Work runs on [DispatcherProvider.default] via [withContext], so filtering/sorting does not
 *   block the UI thread. Tests supply a `DispatcherProvider` double (for example from `:core:testing`).
 */
class FilterAndSortMoviesUseCase @Inject constructor(
    private val dispatchers: DispatcherProvider,
) {

    suspend operator fun invoke(
        movies: List<Movie>,
        genreFilter: Set<Int> = emptySet(),
        sort: SortOption = SortOption.Default,
    ): List<Movie> = withContext(context = dispatchers.default) {
        val filtered = if (genreFilter.isEmpty()) {
            movies
        } else {
            movies.filter { movie -> movie.genreIds.any { it in genreFilter } }
        }

        val ascending = sort.direction == SortDirection.Ascending

        when (sort.criterion) {
            SortCriterion.Popularity ->
                if (ascending) filtered.sortedBy { it.popularity }
                else filtered.sortedByDescending { it.popularity }

            SortCriterion.Title ->
                if (ascending) filtered.sortedBy { it.title.lowercase(Locale.ROOT) }
                else filtered.sortedByDescending { it.title.lowercase(Locale.ROOT) }

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
