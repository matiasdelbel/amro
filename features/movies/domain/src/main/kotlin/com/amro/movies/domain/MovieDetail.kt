package com.amro.movies.domain

import java.time.LocalDate

data class MovieDetail(
    val id: Long,
    val title: String,
    val tagline: String?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val genres: List<Genre>,
    val overview: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val budget: Long,
    val revenue: Long,
    val status: MovieStatus,
    val imdbUrl: String?,
    val runtimeMinutes: Int?,
    val releaseDate: LocalDate?,
    val homepage: String?,
)

enum class MovieStatus { Rumored, Planned, InProduction, PostProduction, Released, Canceled, Unknown }
