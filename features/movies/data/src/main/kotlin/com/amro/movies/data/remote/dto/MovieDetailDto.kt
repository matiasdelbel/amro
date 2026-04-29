package com.amro.movies.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MovieDetailDto(
    val id: Long,
    val title: String = "",
    val tagline: String? = null,
    val overview: String? = null,
    val homepage: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    val genres: List<GenreDto> = emptyList(),
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    val budget: Long = 0,
    val revenue: Long = 0,
    val status: String? = null,
    @SerialName("imdb_id") val imdbId: String? = null,
    val runtime: Int? = null,
    @SerialName("release_date") val releaseDate: String? = null,
)
