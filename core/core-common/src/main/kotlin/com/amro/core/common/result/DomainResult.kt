package com.amro.core.common.result

/**
 * A type-safe wrapper returned by the domain layer.
 *
 * We deliberately avoid leaking Kotlin's [Result] (which relies on exceptions) and instead
 * model failures explicitly with [DomainError]. This makes error handling exhaustive in
 * ViewModels and keeps the domain layer free of framework or transport concerns.
 */
sealed interface DomainResult<out T> {
    data class Success<T>(val value: T) : DomainResult<T>
    data class Failure(val error: DomainError) : DomainResult<Nothing>
}

/**
 * Framework-agnostic error taxonomy. The data layer maps transport/network exceptions into
 * one of these cases so upstream consumers (ViewModel / UI) can present intelligible messages
 * without knowing about HTTP, Ktor, serialization, etc.
 */
sealed class DomainError(open val cause: Throwable? = null) {
    /** No connectivity / host unreachable. */
    data class Network(override val cause: Throwable? = null) : DomainError(cause)

    /** Server returned a non-2xx response. */
    data class Server(val code: Int, override val cause: Throwable? = null) : DomainError(cause)

    /**
     * The server's response could not be parsed (malformed JSON, missing required field,
     * schema drift, etc.). Distinct from [Server] because retrying won't help — the issue is
     * a contract mismatch between client and server.
     */
    data class Parsing(override val cause: Throwable? = null) : DomainError(cause)

    /** Request was cancelled by the caller. */
    data class Cancelled(override val cause: Throwable? = null) : DomainError(cause)

    /** Anything else – unexpected. */
    data class Unknown(override val cause: Throwable? = null) : DomainError(cause)
}

inline fun <T, R> DomainResult<T>.map(transform: (T) -> R): DomainResult<R> = when (this) {
    is DomainResult.Success -> DomainResult.Success(transform(value))
    is DomainResult.Failure -> this
}

inline fun <T> DomainResult<T>.onSuccess(block: (T) -> Unit): DomainResult<T> {
    if (this is DomainResult.Success) block(value)
    return this
}

inline fun <T> DomainResult<T>.onFailure(block: (DomainError) -> Unit): DomainResult<T> {
    if (this is DomainResult.Failure) block(error)
    return this
}
