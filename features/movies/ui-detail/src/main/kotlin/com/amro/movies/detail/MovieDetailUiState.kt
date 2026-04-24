package com.amro.movies.detail

import com.amro.core.common.result.DomainError
import com.amro.movies.domain.MovieDetail

sealed interface MovieDetailUiState {

    data object Loading : MovieDetailUiState

    data class Content(val movie: MovieDetail) : MovieDetailUiState

    data class Error(val error: DomainError) : MovieDetailUiState
}
