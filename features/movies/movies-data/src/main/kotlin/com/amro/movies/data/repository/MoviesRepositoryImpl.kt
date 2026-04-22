package com.amro.movies.data.repository

import com.amro.core.common.dispatcher.DispatcherProvider
import com.amro.core.common.result.DomainError
import com.amro.core.common.result.DomainResult
import com.amro.movies.data.mapper.toDomain
import com.amro.movies.data.remote.MoviesRemoteDataSource
import com.amro.movies.domain.model.Genre
import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.MovieDetail
import com.amro.movies.domain.repository.MoviesRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
internal class MoviesRepositoryImpl @Inject constructor(
    private val remote: MoviesRemoteDataSource,
    private val dispatchers: DispatcherProvider,
) : MoviesRepository {

    /**
     * In-memory genre cache. The genres endpoint is small (~20 entries) and essentially static,
     * so caching it for the process lifetime avoids hitting the network for every trending-list
     * refresh. Protected by a [Mutex] for first-write race safety.
     */
    @Volatile
    private var genreCache: Map<Int, Genre>? = null
    private val genreMutex = Mutex()

    override suspend fun getTrendingTop100(): DomainResult<List<Movie>> =
        withContext(dispatchers.io) {
            runCatchingDomain {
                coroutineScope {
                    val genresDeferred = async { loadGenres() }
                    val pagesDeferred = (1..PAGES_FOR_TOP_100).map { page ->
                        async { remote.getTrendingMovies(page).results }
                    }
                    val genresById = genresDeferred.await()
                    val movies = pagesDeferred.flatMap { it.await() }
                        .asSequence()
                        .distinctBy { it.id }
                        .take(TRENDING_LIMIT)
                        .map { it.toDomain(genresById) }
                        .toList()
                    movies
                }
            }
        }

    override suspend fun getMovieDetail(movieId: Long): DomainResult<MovieDetail> =
        withContext(dispatchers.io) {
            runCatchingDomain { remote.getMovieDetail(movieId).toDomain() }
        }

    private suspend fun loadGenres(): Map<Int, Genre> {
        genreCache?.let { return it }
        return genreMutex.withLock {
            genreCache ?: remote.getMovieGenres().genres
                .associate { it.id to it.toDomain() }
                .also { genreCache = it }
        }
    }

    private inline fun <T> runCatchingDomain(block: () -> T): DomainResult<T> = try {
        DomainResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        DomainResult.Failure(e.toDomainError())
    }

    private fun Throwable.toDomainError(): DomainError = when (this) {
        is HttpRequestTimeoutException -> DomainError.Network(this)
        is IOException -> DomainError.Network(this)
        is ClientRequestException -> DomainError.Server(response.status.value, this)
        is ServerResponseException -> DomainError.Server(response.status.value, this)
        is ResponseException -> DomainError.Server(response.status.value, this)
        else -> DomainError.Unknown(this)
    }

    private companion object {
        const val PAGES_FOR_TOP_100 = 5
        const val TRENDING_LIMIT = 100
    }
}
