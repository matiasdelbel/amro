package com.amro.movies.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amro.core.common.result.DomainResult
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

    private val _state = MutableStateFlow<MovieDetailUiState>(value = MovieDetailUiState.Loading)
    val state: StateFlow<MovieDetailUiState> = _state.asStateFlow()

    init { loadMovie() }

    fun retry() = loadMovie()

    private fun loadMovie() {
        _state.value = MovieDetailUiState.Loading

        viewModelScope.launch {
            _state.value = when (val result = getMovieDetail(movieId)) {
                is DomainResult.Success -> MovieDetailUiState.Content(movie = result.value)
                is DomainResult.Failure -> MovieDetailUiState.Error(error = result.error)
            }
        }
    }
}
