package com.amro.movies.domain.model

import java.time.LocalDate

/**
 * A trending-movie summary, as needed by the list screen.
 *
 * We purposely keep genre ids here plus a resolved [genres] list. The data layer is
 * responsible for resolving the ids against the genres endpoint so the UI can display names
 * without making a per-item network call.
 */
data class Movie(
    val id: Long,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val genreIds: List<Int>,
    val genres: List<Genre>,
    val popularity: Double,
    val releaseDate: LocalDate?,
    val voteAverage: Double,
)
