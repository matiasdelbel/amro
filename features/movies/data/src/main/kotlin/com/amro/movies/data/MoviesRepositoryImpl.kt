package com.amro.movies.data

import com.amro.core.coroutine.dispatcher.DispatcherProvider
import com.amro.core.domain.DomainError
import com.amro.core.domain.DomainResult
import com.amro.movies.data.remote.MoviesRemoteDataSource
import com.amro.movies.domain.Genre
import com.amro.movies.domain.Movie
import com.amro.movies.domain.MovieDetail
import com.amro.movies.domain.repository.MoviesRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MoviesRepositoryImpl @Inject constructor(
    private val remote: MoviesRemoteDataSource,
    private val dispatchers: DispatcherProvider,
) : MoviesRepository {

    /**
     * In-memory genre cache. The genres endpoint is small (~20 entries) and essentially static,
     * so caching it for the process lifetime avoids hitting the network for every trending-list
     * refresh. Protected by a [kotlinx.coroutines.sync.Mutex] for first-write race safety.
     */
    @Volatile
    private var genreCache: Map<Int, Genre>? = null
    private val genreMutex = Mutex()

    /**
     * Fetches up to 100 trending movies in parallel.
     *
     * **All-or-nothing semantics**: genres + every page request runs as siblings inside a single
     * [coroutineScope], so any single failure (e.g. page 4 returns 500, or the genres call
     * times out) cancels the others and surfaces as a [DomainResult.Failure]. This is a
     * deliberate trade-off — the spec asks for "top 100" and a partial list (e.g. only 60
     * movies because page 4 was missing) would be confusing UX. If we ever want graceful
     * degradation, this is the place to introduce it.
     */
    override suspend fun getTrendingTop100(): DomainResult<List<Movie>> = runCatchingDomain {
        coroutineScope {
            val genresDeferred = async { loadGenres() }
            val pagesDeferred = (1..PAGES_FOR_TOP_100).map { page ->
                async { remote.getTrendingMovies(page).results }
            }
            val genresById = genresDeferred.await()
            val movies = pagesDeferred
                .flatMap { it.await() }
                .asSequence()
                .distinctBy { it.id }
                .take(TRENDING_LIMIT)
                .map { it.toDomain(genresById) }
                .toList()

            movies
        }
    }

    override suspend fun getMovieDetail(movieId: Long): DomainResult<MovieDetail> = withContext(dispatchers.io) {
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

    /**
     * Wraps [block] and converts any [Throwable] into a [DomainResult.Failure].
     *
     * Two subtleties enforced here:
     * - [CancellationException] is rethrown so structured concurrency works correctly
     *   (a cancelled child must not be reported back as a "failure").
     * - JVM [Error]s (OOM, StackOverflow, LinkageError, …) are rethrown as well: the process
     *   is in an undefined state and pretending we recovered would mask real problems.
     */
    private inline fun <T> runCatchingDomain(block: () -> T): DomainResult<T> = try {
        DomainResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        if (e is Error) throw e
        DomainResult.Failure(e.toDomainError())
    }

    private fun Throwable.toDomainError(): DomainError = when (this) {
        is HttpRequestTimeoutException -> DomainError.Network(this)
        is IOException -> DomainError.Network(this)
        is ClientRequestException -> DomainError.Server(response.status.value, this)
        is ServerResponseException -> DomainError.Server(response.status.value, this)
        is ResponseException -> DomainError.Server(response.status.value, this)
        // Malformed JSON / schema drift: distinct from `Server` because retrying won't help —
        // the contract between client and server is broken. Ktor's ContentNegotiation wraps
        // every kotlinx-serialization failure as `ContentConvertException` *before* it leaves
        // the client (e.g. `JsonConvertException`), so we have to match that wrapper rather
        // than the underlying `SerializationException` to actually catch wire-shape mismatches.
        is ContentConvertException -> DomainError.Parsing(this)
        is SerializationException -> DomainError.Parsing(this)
        else -> DomainError.Unknown(this)
    }

    private companion object {
        // Hard-coded to satisfy the MVP spec ("top 100"). TMDB serves trending in pages of 20
        // and currently always has many more than 5 pages, so we don't bother making this
        // adaptive yet.
        const val PAGES_FOR_TOP_100 = 5
        const val TRENDING_LIMIT = 100
    }
}
