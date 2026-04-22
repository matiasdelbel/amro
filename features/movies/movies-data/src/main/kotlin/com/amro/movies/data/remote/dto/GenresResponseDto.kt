package com.amro.movies.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class GenresResponseDto(
    val genres: List<GenreDto> = emptyList(),
)

@Serializable
internal data class GenreDto(
    val id: Int,
    val name: String,
)
