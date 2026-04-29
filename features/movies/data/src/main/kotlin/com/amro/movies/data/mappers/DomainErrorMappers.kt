package com.amro.movies.data.mappers

import com.amro.core.domain.DomainError
import com.amro.core.domain.DomainResult
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import java.io.IOException

/**
 * Wraps [block] and converts any [Throwable] into a [DomainResult.Failure].
 *
 * Two subtleties enforced here:
 * - [CancellationException] is rethrown so structured concurrency works correctly
 *   (a cancelled child must not be reported back as a "failure").
 * - JVM [Error]s (OOM, StackOverflow, LinkageError, …) are rethrown as well: the process
 *   is in an undefined state and pretending we recovered would mask real problems.
 *
 * Lives as a top-level `internal` helper (rather than a private method on
 * `MoviesRepositoryImpl`) so its mapping rules can be unit-tested directly without spinning
 * up a Ktor [io.ktor.client.HttpClient] / `MockEngine` for every error case.
 */
internal inline fun <T> runCatchingDomain(block: () -> T): DomainResult<T> = try {
    DomainResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    if (e is Error) throw e
    DomainResult.Failure(e.toDomainError())
}

/**
 * Maps a transport-layer [Throwable] into a framework-agnostic [DomainError] case.
 *
 * Order matters: more specific Ktor subtypes are matched before [ResponseException], and
 * [ContentConvertException] is matched before [SerializationException] because Ktor's
 * `ContentNegotiation` plugin wraps every kotlinx-serialization failure as
 * `ContentConvertException` (e.g. `JsonConvertException`) before it leaves the client.
 */
internal fun Throwable.toDomainError(): DomainError = when (this) {
    is HttpRequestTimeoutException -> DomainError.Network(this)
    is IOException -> DomainError.Network(this)
    is ClientRequestException -> DomainError.Server(response.status.value, this)
    is ServerResponseException -> DomainError.Server(response.status.value, this)
    is ResponseException -> DomainError.Server(response.status.value, this)
    is ContentConvertException -> DomainError.Parsing(this)
    is SerializationException -> DomainError.Parsing(this)
    else -> DomainError.Unknown(this)
}
