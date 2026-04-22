package com.amro.movies.listing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amro.designsystem.components.ErrorState
import com.amro.designsystem.components.LoadingState
import com.amro.movies.domain.model.Movie

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesListingRoute(
    onMovieClick: (movieId: Long) -> Unit,
    viewModel: MoviesListingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MoviesListingScreen(
        state = state,
        onMovieClick = onMovieClick,
        onRetry = viewModel::retry,
        onRefresh = viewModel::refresh,
        onToggleGenre = viewModel::toggleGenre,
        onClearGenres = viewModel::clearGenreFilter,
        onUpdateSort = viewModel::updateSort,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MoviesListingScreen(
    state: MoviesListingUiState,
    onMovieClick: (Long) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onToggleGenre: (Int) -> Unit,
    onClearGenres: () -> Unit,
    onUpdateSort: (com.amro.movies.domain.usecase.SortCriterion, com.amro.movies.domain.usecase.SortDirection) -> Unit,
) {
    var showSortSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trending this week") },
                actions = {
                    IconButton(onClick = { showSortSheet = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                    }
                    IconButton(onClick = onRefresh, enabled = !state.isLoading && !state.isRefreshing) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && !state.hasContent -> LoadingState()
                state.errorMessage != null && !state.hasContent -> ErrorState(
                    message = state.errorMessage,
                    onRetry = onRetry,
                )
                else -> MoviesListingContent(
                    state = state,
                    onMovieClick = onMovieClick,
                    onToggleGenre = onToggleGenre,
                    onClearGenres = onClearGenres,
                )
            }
        }

        if (showSortSheet) {
            SortOptionsSheet(
                current = state.sortOption,
                onDismiss = { showSortSheet = false },
                onApply = { criterion, direction ->
                    onUpdateSort(criterion, direction)
                    showSortSheet = false
                },
            )
        }
    }
}

@Composable
private fun MoviesListingContent(
    state: MoviesListingUiState,
    onMovieClick: (Long) -> Unit,
    onToggleGenre: (Int) -> Unit,
    onClearGenres: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        GenreFilterRow(
            genres = state.availableGenres,
            selected = state.selectedGenreIds,
            onToggle = onToggleGenre,
            onClear = onClearGenres,
        )
        if (state.visibleMovies.isEmpty() && state.hasContent) {
            EmptyFilterState(modifier = Modifier.fillMaxWidth().padding(24.dp))
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(items = state.visibleMovies, key = Movie::id) { movie ->
                    MovieListItem(movie = movie, onClick = { onMovieClick(movie.id) })
                }
            }
        }
    }
}

@Composable
private fun EmptyFilterState(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "No movies match the selected genres.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "Try removing a genre to see more results.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
