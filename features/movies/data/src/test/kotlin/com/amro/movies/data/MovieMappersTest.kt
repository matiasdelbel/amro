package com.amro.movies.data

import com.amro.movies.data.remote.dto.GenreDto
import com.amro.movies.data.remote.dto.MovieDetailDto
import com.amro.movies.data.remote.dto.TrendingMovieDto
import com.amro.movies.domain.Genre
import com.amro.movies.domain.MovieStatus
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class MovieMappersTest {

    @Test
    fun `invalid release date yields null`() {
        val dto = TrendingMovieDto(id = 1, releaseDate = "not-a-date", genreIds = emptyList())
        val movie = dto.toDomain(genresById = emptyMap())
        assertThat(movie.releaseDate).isNull()
    }

    @Test
    fun `genre ids resolve via provided lookup`() {
        val dto = TrendingMovieDto(id = 1, genreIds = listOf(1, 2, 999))
        val movie = dto.toDomain(
            genresById = mapOf(
                1 to Genre(1, "Action"),
                2 to Genre(2, "Comedy"),
            ),
        )
        assertThat(movie.genres.map { it.name }).containsExactly("Action", "Comedy").inOrder()
    }

    @Test
    fun `blank tagline and overview are normalized to null`() {
        val dto = MovieDetailDto(id = 1, tagline = "  ", overview = "", status = "Released")
        val detail = dto.toDomain()
        assertThat(detail.tagline).isNull()
        assertThat(detail.overview).isNull()
    }

    @Test
    fun `imdb url is built from imdb_id`() {
        val dto = MovieDetailDto(id = 1, imdbId = "tt7654321", status = "Released")
        assertThat(dto.toDomain().imdbUrl).isEqualTo("https://www.imdb.com/title/tt7654321")
    }

    @Test
    fun `missing imdb id yields null url`() {
        val dto = MovieDetailDto(id = 1, imdbId = null)
        assertThat(dto.toDomain().imdbUrl).isNull()
    }

    @Test
    fun `unknown status strings map to Unknown`() {
        val dto = MovieDetailDto(id = 1, status = "Something weird")
        assertThat(dto.toDomain().status).isEqualTo(MovieStatus.Unknown)
    }

    @Test
    fun `status with surrounding whitespace is trimmed before mapping`() {
        // Defensive `trim()` in the mapper protects against an upstream typo silently
        // demoting every detail to `Unknown`.
        val dto = MovieDetailDto(id = 1, status = "  Released  ")
        assertThat(dto.toDomain().status).isEqualTo(MovieStatus.Released)
    }

    @Test
    fun `known release date is parsed`() {
        val dto = MovieDetailDto(id = 1, releaseDate = "2024-05-17")
        assertThat(dto.toDomain().releaseDate).isEqualTo(LocalDate.of(2024, 5, 17))
    }

    @Test
    fun `genre dto mapping is transparent`() {
        val g = GenreDto(id = 5, name = "Drama").toDomain()
        assertThat(g).isEqualTo(Genre(5, "Drama"))
    }
}
