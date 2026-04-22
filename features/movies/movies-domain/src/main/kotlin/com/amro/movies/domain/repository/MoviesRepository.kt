package com.amro.movies.domain.repository

import com.amro.core.common.result.DomainResult
import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.MovieDetail

/**
 * Repository contract owned by the domain layer. The data layer provides the implementation.
 *
 * No framework, HTTP, or serialization types should appear in this interface. Signatures use
 * plain suspend functions + [DomainResult] – the data layer maps every transport exception
 * into a [com.amro.core.common.result.DomainError].
 */
interface MoviesRepository {

    /**
     * Returns **up to 100 trending movies of the week**, with their genres resolved.
     *
     * TMDB paginates at 20 items/page, so the implementation fetches 5 pages sequentially and
     * concatenates them (de-duplicating by id). Requirement: top 100 trending list.
     */
    suspend fun getTrendingTop100(): DomainResult<List<Movie>>

    /** Fetches the full detail payload for a single movie by its TMDB id. */
    suspend fun getMovieDetail(movieId: Long): DomainResult<MovieDetail>
}
