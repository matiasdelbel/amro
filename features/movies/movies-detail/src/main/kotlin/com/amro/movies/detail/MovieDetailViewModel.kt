package com.amro.movies.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amro.core.common.result.DomainError
import com.amro.core.common.result.DomainResult
import com.amro.movies.detail.navigation.MovieDetailArgs
import com.amro.movies.domain.usecase.GetMovieDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMovieDetail: GetMovieDetailUseCase,
) : ViewModel() {

    private val movieId: Long = MovieDetailArgs(savedStateHandle).movieId

    private val _state = MutableStateFlow<MovieDetailUiState>(MovieDetailUiState.Loading)
    val state: StateFlow<MovieDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        _state.value = MovieDetailUiState.Loading
        viewModelScope.launch {
            _state.value = when (val result = getMovieDetail(movieId)) {
                is DomainResult.Success -> MovieDetailUiState.Content(result.value)
                is DomainResult.Failure -> MovieDetailUiState.Error(result.error.toMessage())
            }
        }
    }

    private fun DomainError.toMessage(): String = when (this) {
        is DomainError.Network -> "No internet connection. Check your network and try again."
        is DomainError.Server -> "The movie could not be loaded (code $code)."
        is DomainError.Cancelled -> "Request cancelled."
        is DomainError.Unknown -> "Unexpected error. Please try again."
    }
}
