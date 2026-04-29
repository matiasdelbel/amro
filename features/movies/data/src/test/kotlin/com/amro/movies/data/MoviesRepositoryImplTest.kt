package com.amro.movies.data

import com.amro.core.domain.DomainError
import com.amro.core.domain.DomainResult
import com.amro.core.testing.TestDispatcherProvider
import com.amro.movies.data.remote.MoviesRemoteDataSource
import com.google.common.truth.Truth.assertThat
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesRepositoryImplTest {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Test
    fun `getTrendingTop100 returns mapped movies with resolved genres`() = runTest {
        val handler = fakeTmdb(
            genres = """{"genres":[{"id":28,"name":"Action"},{"id":35,"name":"Comedy"}]}""",
            // Same id across all 5 pages → exercises `distinctBy { it.id }` dedup.
            trendingByPage = { """{"page":1,"results":[{"id":1,"title":"Hello","genre_ids":[28],"popularity":42.0}]}""" },
        )
        val repo = buildRepo(handler)

        val result = repo.getTrendingTop100() as DomainResult.Success

        assertThat(result.value).hasSize(1)
        val movie = result.value.single()
        assertThat(movie.title).isEqualTo("Hello")
        assertThat(movie.genres.single().name).isEqualTo("Action")
        assertThat(movie.popularity).isEqualTo(42.0)
    }

    @Test
    fun `getTrendingTop100 returns Network error for IO failure`() = runTest {
        val engine = MockEngine { throw UnknownHostException("no network") }
        val client = HttpClient(engine) { install(ContentNegotiation) { json(json) } }
        val repo = MoviesRepositoryImpl(MoviesRemoteDataSource(client), TestDispatcherProvider())

        val result = repo.getTrendingTop100()

        assertThat(result).isInstanceOf(DomainResult.Failure::class.java)
        assertThat((result as DomainResult.Failure).error).isInstanceOf(DomainError.Network::class.java)
    }

    @Test
    fun `getTrendingTop100 returns Server error for non-2xx response`() = runTest {
        val engine = MockEngine { respondError(HttpStatusCode.InternalServerError) }
        val repo = buildRepo(engine)

        val result = repo.getTrendingTop100() as DomainResult.Failure

        val error = result.error as DomainError.Server
        assertThat(error.code).isEqualTo(500)
    }

    @Test
    fun `getTrendingTop100 returns Parsing error when payload is malformed`() = runTest {
        // Missing required `id` field → kotlinx.serialization throws SerializationException,
        // which the repo maps to DomainError.Parsing (distinct from Server because retrying
        // won't help).
        val handler = fakeTmdb(
            genres = """{"genres":[]}""",
            trendingByPage = { """{"page":1,"results":[{"title":"Hello","genre_ids":[],"popularity":42.0}]}""" },
        )
        val repo = buildRepo(handler)

        val result = repo.getTrendingTop100() as DomainResult.Failure

        assertThat(result.error).isInstanceOf(DomainError.Parsing::class.java)
    }

    @Test
    fun `getTrendingTop100 reuses the cached genre map across calls`() = runTest {
        val genreCalls = AtomicInteger(0)
        val handler = fakeTmdb(
            onGenres = { genreCalls.incrementAndGet() },
            genres = """{"genres":[{"id":28,"name":"Action"}]}""",
            trendingByPage = { """{"page":1,"results":[{"id":1,"title":"M","genre_ids":[28],"popularity":1.0}]}""" },
        )
        val repo = buildRepo(handler)

        repeat(3) { repo.getTrendingTop100() }

        // First call populates the cache; subsequent calls must NOT hit `genre/movie/list`.
        // This documents the intent of the `@Volatile` + `Mutex` cache and protects it from
        // accidental removal in a future refactor.
        assertThat(genreCalls.get()).isEqualTo(1)
    }

    @Test
    fun `getTrendingTop100 fails the whole call when any single page errors out`() = runTest {
        // Pages 1-3 return a valid payload; page 4 returns 500. Pinning the documented
        // "all-or-nothing" semantics: a partial 60-movie list would be confusing UX, so the
        // first failing sibling must cancel the others and surface as a Failure.
        val handler = MockEngine { request ->
            val path = request.url.encodedPath
            when {
                path.endsWith("genre/movie/list") ->
                    respondJson("""{"genres":[{"id":28,"name":"Action"}]}""")
                path.contains("trending/movie/week") -> {
                    val page = request.url.parameters["page"]?.toInt() ?: 1
                    if (page == 4) respondError(HttpStatusCode.InternalServerError)
                    else respondJson(
                        """{"page":$page,"results":[{"id":${page * 10},"title":"M$page","genre_ids":[28],"popularity":1.0}]}"""
                    )
                }
                else -> respondJson("{}")
            }
        }
        val repo = buildRepo(handler)

        val result = repo.getTrendingTop100() as DomainResult.Failure

        val error = result.error as DomainError.Server
        assertThat(error.code).isEqualTo(500)
    }

    @Test
    fun `getTrendingTop100 deduplicates and truncates across multiple pages`() = runTest {
        // Each page returns 25 distinct movies (page N: ids N*100 + 0..24). With 5 pages
        // that's 125 movies → after `take(100)` we expect exactly 100, all distinct.
        val handler = fakeTmdb(
            genres = """{"genres":[]}""",
            trendingByPage = { page ->
                val results = (0 until 25).joinToString(",") { i ->
                    val id = page * 100 + i
                    """{"id":$id,"title":"M$id","genre_ids":[],"popularity":${(25 - i).toDouble()}}"""
                }
                """{"page":$page,"results":[$results]}"""
            },
        )
        val repo = buildRepo(handler)

        val result = repo.getTrendingTop100() as DomainResult.Success

        assertThat(result.value).hasSize(100)
        assertThat(result.value.map { it.id }.toSet()).hasSize(100)
    }

    @Test
    fun `runCatchingDomain rethrows CancellationException instead of converting to Failure`() = runTest {
        // CancellationException is special: catching it as if it were a regular failure would
        // break structured concurrency (a cancelled child must signal cancellation upstream).
        // We force the failure path by having the mock engine throw CE directly; the repo must
        // rethrow it rather than wrap it in `DomainResult.Failure`. `runCatching` here
        // intentionally relies on its (well-known) habit of catching CE — that's the only way
        // to assert "the suspend function threw CE" without taking down the test scope.
        val engine = MockEngine { throw CancellationException("simulated cancel") }
        val repo = buildRepo(engine)

        val thrown = runCatching { repo.getTrendingTop100() }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(CancellationException::class.java)
    }

    @Test
    fun `getMovieDetail returns Server error for non-2xx response`() = runTest {
        val engine = MockEngine { respondError(HttpStatusCode.NotFound) }
        val repo = buildRepo(engine)

        val result = repo.getMovieDetail(99) as DomainResult.Failure

        val error = result.error as DomainError.Server
        assertThat(error.code).isEqualTo(404)
    }

    @Test
    fun `getMovieDetail returns Network error for IO failure`() = runTest {
        val engine = MockEngine { throw UnknownHostException("no network") }
        val repo = buildRepo(engine)

        val result = repo.getMovieDetail(99) as DomainResult.Failure

        assertThat(result.error).isInstanceOf(DomainError.Network::class.java)
    }

    @Test
    fun `getMovieDetail maps imdb url and status correctly`() = runTest {
        val handler = MockEngine {
            respondJson("""{"id":99,"title":"Movie","imdb_id":"tt0000001","status":"Released","genres":[]}""")
        }
        val repo = buildRepo(handler)

        val result = repo.getMovieDetail(99) as DomainResult.Success

        assertThat(result.value.imdbUrl).isEqualTo("https://www.imdb.com/title/tt0000001")
        assertThat(result.value.status.name).isEqualTo("Released")
    }

    private fun buildRepo(
        engine: MockEngine,
        dispatchers: TestDispatcherProvider = TestDispatcherProvider(),
    ): MoviesRepositoryImpl {
        val client = HttpClient(engine) {
            expectSuccess = true
            install(ContentNegotiation) { json(json) }
        }
        return MoviesRepositoryImpl(MoviesRemoteDataSource(client), dispatchers)
    }

    private fun fakeTmdb(
        genres: String,
        trendingByPage: (Int) -> String,
        onGenres: () -> Unit = {},
    ): MockEngine = MockEngine { request ->
        val path = request.url.encodedPath
        val body = when {
            path.endsWith("genre/movie/list") -> {
                onGenres()
                genres
            }
            path.contains("trending/movie/week") -> {
                val page = request.url.parameters["page"]?.toInt() ?: 1
                trendingByPage(page)
            }
            else -> "{}"
        }
        respondJson(body)
    }

    private fun MockRequestHandleScope.respondJson(body: String): HttpResponseData = respond(
        content = body,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
    )
}
