package com.amro.movies.data.remote

import com.amro.movies.data.remote.dto.GenresResponseDto
import com.amro.movies.data.remote.dto.MovieDetailDto
import com.amro.movies.data.remote.dto.TrendingMoviesResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

/**
 * Thin wrapper around [HttpClient] that only knows about TMDB endpoints.
 *
 * Kept `internal` to the module so the only thing crossing the data-module boundary is the
 * repository interface from `movies:domain`.
 */
internal class MoviesRemoteDataSource @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun getTrendingMovies(page: Int): TrendingMoviesResponseDto =
        client.get("trending/movie/week") {
            parameter("page", page)
        }.body()

    suspend fun getMovieGenres(): GenresResponseDto =
        client.get("genre/movie/list").body()

    suspend fun getMovieDetail(movieId: Long): MovieDetailDto =
        client.get("movie/$movieId").body()
}
