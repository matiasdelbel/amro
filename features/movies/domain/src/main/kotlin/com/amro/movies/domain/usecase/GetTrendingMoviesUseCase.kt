package com.amro.movies.domain.usecase

import com.amro.core.common.result.DomainResult
import com.amro.movies.domain.Movie
import com.amro.movies.domain.repository.MoviesRepository
import javax.inject.Inject

/**
 * Thin wrapper around [MoviesRepository.getTrendingTop100].
 * Exists as a use case so the UI layer depends on intent rather than on the repository directly,
 * which makes future additions (caching, merging with another source) transparent.
 */
class GetTrendingMoviesUseCase @Inject constructor(
    private val repository: MoviesRepository,
) {
    suspend operator fun invoke(): DomainResult<List<Movie>> = repository.getTrendingTop100()
}
