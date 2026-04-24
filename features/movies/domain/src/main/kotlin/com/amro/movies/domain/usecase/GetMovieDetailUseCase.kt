package com.amro.movies.domain.usecase

import com.amro.core.domain.DomainResult
import com.amro.movies.domain.MovieDetail
import com.amro.movies.domain.repository.MoviesRepository
import javax.inject.Inject

class GetMovieDetailUseCase @Inject constructor(
    private val repository: MoviesRepository,
) {
    suspend operator fun invoke(movieId: Long): DomainResult<MovieDetail> =
        repository.getMovieDetail(movieId)
}
