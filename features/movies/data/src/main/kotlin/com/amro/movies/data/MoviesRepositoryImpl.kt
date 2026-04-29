package com.amro.movies.data

import com.amro.core.coroutine.dispatcher.DispatcherProvider
import com.amro.core.domain.DomainResult
import com.amro.movies.data.mappers.runCatchingDomain
import com.amro.movies.data.mappers.toDomain
import com.amro.movies.data.remote.MoviesRemoteDataSource
import com.amro.movies.domain.Genre
import com.amro.movies.domain.Movie
import com.amro.movies.domain.MovieDetail
import com.amro.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
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
    override suspend fun getTrendingTop100(): DomainResult<List<Movie>> = withContext(dispatchers.io) {
        runCatchingDomain {
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
    }

    override suspend fun getMovieDetail(movieId: Long): DomainResult<MovieDetail> = withContext(dispatchers.io) {
        runCatchingDomain { remote.getMovieDetail(movieId).toDomain() }
    }

    private suspend fun loadGenres(): Map<Int, Genre> {
        genreCache?.let { return it }
        return genreMutex.withLock {
            // Re-check inside the lock: another caller may have populated the cache while we
            // were waiting on the mutex, in which case we want to reuse it rather than fire a
            // second `genre/movie/list` request.
            genreCache ?: remote.getMovieGenres().genres
                .associate { it.id to it.toDomain() }
                .also { genreCache = it }
        }
    }

    private companion object {
        // Hard-coded to satisfy the MVP spec ("top 100"). TMDB serves trending in pages of 20
        // and currently always has many more than 5 pages, so we don't bother making this
        // adaptive yet.
        const val PAGES_FOR_TOP_100 = 5
        const val TRENDING_LIMIT = 100
    }
}
