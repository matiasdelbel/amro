package com.amro.movies.data.mapper

import com.amro.core.network.TmdbConfig
import com.amro.movies.data.remote.dto.GenreDto
import com.amro.movies.data.remote.dto.MovieDetailDto
import com.amro.movies.data.remote.dto.TrendingMovieDto
import com.amro.movies.domain.Genre
import com.amro.movies.domain.Movie
import com.amro.movies.domain.MovieDetail
import com.amro.movies.domain.MovieStatus
import java.time.LocalDate
import java.time.format.DateTimeParseException

internal fun GenreDto.toDomain(): Genre = Genre(id = id, name = name)

internal fun TrendingMovieDto.toDomain(genresById: Map<Int, Genre>): Movie = Movie(
    id = id,
    title = title,
    posterUrl = posterPath?.toPosterUrl(),
    backdropUrl = backdropPath?.toBackdropUrl(),
    genreIds = genreIds,
    genres = genreIds.mapNotNull { genresById[it] },
    popularity = popularity,
    releaseDate = parseLocalDate(releaseDate),
    voteAverage = voteAverage,
)

internal fun MovieDetailDto.toDomain(): MovieDetail = MovieDetail(
    id = id,
    title = title,
    tagline = tagline?.takeIf { it.isNotBlank() },
    posterUrl = posterPath?.toPosterUrl(),
    backdropUrl = backdropPath?.toBackdropUrl(),
    genres = genres.map { it.toDomain() },
    overview = overview?.takeIf { it.isNotBlank() },
    voteAverage = voteAverage,
    voteCount = voteCount,
    budget = budget,
    revenue = revenue,
    status = status.toMovieStatus(),
    imdbUrl = imdbId?.let { "https://www.imdb.com/title/$it" },
    runtimeMinutes = runtime,
    releaseDate = parseLocalDate(releaseDate),
    homepage = homepage?.takeIf { it.isNotBlank() },
)

internal fun String.toPosterUrl(): String =
    "${TmdbConfig.IMAGE_BASE_URL}${TmdbConfig.POSTER_SIZE}$this"

internal fun String.toBackdropUrl(): String =
    "${TmdbConfig.IMAGE_BASE_URL}${TmdbConfig.BACKDROP_SIZE}$this"

private fun parseLocalDate(raw: String?): LocalDate? {
    if (raw.isNullOrBlank()) return null
    return try {
        LocalDate.parse(raw)
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun String?.toMovieStatus(): MovieStatus = when (this) {
    "Rumored" -> MovieStatus.Rumored
    "Planned" -> MovieStatus.Planned
    "In Production" -> MovieStatus.InProduction
    "Post Production" -> MovieStatus.PostProduction
    "Released" -> MovieStatus.Released
    "Canceled" -> MovieStatus.Canceled
    else -> MovieStatus.Unknown
}
