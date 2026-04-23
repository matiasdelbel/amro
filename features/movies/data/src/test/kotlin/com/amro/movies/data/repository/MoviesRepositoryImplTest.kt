package com.amro.movies.data.repository

import com.amro.core.common.result.DomainError
import com.amro.core.common.result.DomainResult
import com.amro.core.testing.TestDispatcherProvider
import com.amro.movies.data.remote.MoviesRemoteDataSource
import com.google.common.truth.Truth.assertThat
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
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
            trendingByPage = { """{"page":1,"results":[{"id":1,"title":"Hello","genre_ids":[28],"popularity":42.0}]}""" },
        )
        val repo = buildRepo(handler)

        val result = repo.getTrendingTop100() as DomainResult.Success

        assertThat(result.value).hasSize(1) // Deduplicated: same id across 5 pages
        val movie = result.value.single()
        assertThat(movie.title).isEqualTo("Hello")
        assertThat(movie.genres.single().name).isEqualTo("Action")
        assertThat(movie.popularity).isEqualTo(42.0)
    }

    @Test
    fun `getTrendingTop100 returns Network error for IO failure`() = runTest {
        val engine = MockEngine { throw java.net.UnknownHostException("no network") }
        val client = HttpClient(engine) { install(ContentNegotiation) { json(json) } }
        val repo = MoviesRepositoryImpl(MoviesRemoteDataSource(client), TestDispatcherProvider())

        val result = repo.getTrendingTop100()

        assertThat(result).isInstanceOf(DomainResult.Failure::class.java)
        assertThat((result as DomainResult.Failure).error).isInstanceOf(DomainError.Network::class.java)
    }

    @Test
    fun `getTrendingTop100 returns Server error for non-2xx response`() = runTest {
        val engine = MockEngine { respondError(HttpStatusCode.InternalServerError) }
        val client = HttpClient(engine) {
            expectSuccess = true
            install(ContentNegotiation) { json(json) }
        }
        val repo = MoviesRepositoryImpl(MoviesRemoteDataSource(client), TestDispatcherProvider())

        val result = repo.getTrendingTop100() as DomainResult.Failure
        val error = result.error as DomainError.Server
        assertThat(error.code).isEqualTo(500)
    }

    @Test
    fun `getMovieDetail maps imdb url and status correctly`() = runTest {
        val handler = MockEngine { request ->
            val body = """{"id":99,"title":"Movie","imdb_id":"tt0000001","status":"Released","genres":[]}"""
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        }
        val repo = buildRepo(handler)

        val result = repo.getMovieDetail(99) as DomainResult.Success

        assertThat(result.value.imdbUrl).isEqualTo("https://www.imdb.com/title/tt0000001")
        assertThat(result.value.status.name).isEqualTo("Released")
    }

    private fun buildRepo(engine: MockEngine): MoviesRepositoryImpl {
        val client = HttpClient(engine) {
            expectSuccess = true
            install(ContentNegotiation) { json(json) }
        }
        return MoviesRepositoryImpl(MoviesRemoteDataSource(client), TestDispatcherProvider())
    }

    private fun fakeTmdb(
        genres: String,
        trendingByPage: (Int) -> String,
    ): MockEngine = MockEngine { request ->
        val path = request.url.encodedPath
        val body = when {
            path.endsWith("genre/movie/list") -> genres
            path.contains("trending/movie/week") -> {
                val page = request.url.parameters["page"]?.toInt() ?: 1
                trendingByPage(page)
            }
            else -> "{}"
        }
        respond(
            content = body,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
    }
}
