package com.amro.core.network

/**
 * Static TMDB endpoint configuration exposed to the data layer. Keeping this in a single
 * place means the `movies-data` module doesn't have to know about BuildConfig or any
 * Android-specific plumbing.
 */
object TmdbConfig {
    const val API_KEY: String = BuildConfig.TMDB_API_KEY
    const val BASE_URL: String = BuildConfig.TMDB_BASE_URL
    const val IMAGE_BASE_URL: String = BuildConfig.TMDB_IMAGE_BASE_URL

    /** Default poster size used by the UI; TMDB supports w92, w154, w185, w342, w500, w780, original. */
    const val POSTER_SIZE: String = "w500"
    const val BACKDROP_SIZE: String = "w780"
}
