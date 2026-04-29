package com.amro.movies.data.mappers

import com.amro.core.domain.DomainError
import com.amro.core.domain.DomainResult
import com.google.common.truth.Truth.assertThat
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.serialization.JsonConvertException
import java.io.IOException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import org.junit.Test

/**
 * Direct unit tests for the `Throwable.toDomainError()` mapping helper and
 * `runCatchingDomain { … }`. These cover the same rules that
 * [com.amro.movies.data.MoviesRepositoryImplTest] exercises end-to-end through `MockEngine`, but without the
 * cost of standing up a Ktor pipeline for each error case — which means they document the
 * mapping at a glance and stay valid even if the repository is rewritten on top of a
 * different transport.
 */
class DomainErrorMappersTest {

    @Test
    fun `IOException maps to Network`() {
        val cause = UnknownHostException("no dns")
        assertThat(cause.toDomainError()).isEqualTo(DomainError.Network(cause))
    }

    @Test
    fun `HttpRequestTimeoutException maps to Network`() {
        val cause = HttpRequestTimeoutException(url = "https://example.com", timeoutMillis = 1L)
        assertThat(cause.toDomainError()).isEqualTo(DomainError.Network(cause))
    }

    @Test
    fun `ContentConvertException maps to Parsing`() {
        // This is the wrapper that Ktor's ContentNegotiation throws around any
        // kotlinx-serialization failure (JsonConvertException is its concrete subtype). The
        // repository's end-to-end "malformed payload" test exercises this indirectly via a
        // missing-field SerializationException; here we pin the *direct* mapping rule, so a
        // future refactor that changes the order of the `when` branches can't accidentally
        // reorder ContentConvertException below SerializationException without this test
        // staying green by coincidence.
        val cause = JsonConvertException("missing field")
        val result = cause.toDomainError()
        assertThat(result).isInstanceOf(DomainError.Parsing::class.java)
        assertThat((result as DomainError.Parsing).cause).isSameInstanceAs(cause)
    }

    @Test
    fun `bare SerializationException also maps to Parsing`() {
        val cause = SerializationException("schema drift")
        assertThat(cause.toDomainError()).isInstanceOf(DomainError.Parsing::class.java)
    }

    @Test
    fun `unknown throwable maps to Unknown`() {
        val cause = IllegalStateException("boom")
        assertThat(cause.toDomainError()).isEqualTo(DomainError.Unknown(cause))
    }

    @Test
    fun `runCatchingDomain returns Success when block completes normally`() {
        val result = runCatchingDomain { 42 }
        assertThat(result).isEqualTo(DomainResult.Success(42))
    }

    @Test
    fun `runCatchingDomain wraps thrown Throwable as Failure with mapped DomainError`() {
        val cause = IOException("connection reset")
        val result = runCatchingDomain { throw cause }
        assertThat(result).isInstanceOf(DomainResult.Failure::class.java)
        assertThat((result as DomainResult.Failure).error).isEqualTo(DomainError.Network(cause))
    }

    @Test(expected = CancellationException::class)
    fun `runCatchingDomain rethrows CancellationException so structured concurrency works`() {
        // CancellationException must NEVER be turned into a Failure: a cancelled child has to
        // signal cancellation upstream to its scope, otherwise sibling coroutines won't get
        // the signal that they should stop too. The repository test pins this end-to-end
        // through the MockEngine; here we pin the helper directly.
        runCatchingDomain { throw CancellationException("simulated cancel") }
    }

    @Test(expected = OutOfMemoryError::class)
    fun `runCatchingDomain rethrows JVM Error instead of swallowing it`() {
        // OOM/StackOverflow/LinkageError leave the JVM in an undefined state — pretending we
        // recovered would mask real problems and confuse crash reporting.
        runCatchingDomain { throw OutOfMemoryError("simulated") }
    }
}
