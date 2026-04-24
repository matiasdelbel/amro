package com.amro.movies.listing

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.amro.core.common.result.DomainError
import com.amro.designsystem.components.ErrorState
import com.amro.designsystem.components.LoadingState
import com.amro.designsystem.theme.PreviewSurface
import com.amro.designsystem.theme.spacers
import com.amro.movies.domain.Genre
import com.amro.movies.domain.Movie
import com.amro.movies.domain.SortCriterion
import com.amro.movies.domain.SortDirection
import com.amro.movies.domain.SortOption
import java.time.LocalDate

@Composable
fun MoviesListingScreen(
    onMovieClick: (movieId: Long) -> Unit,
    viewModel: MoviesListingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MoviesListingScaffold(
        state = state,
        onMovieClick = onMovieClick,
        onRetry = viewModel::retry,
        onRefresh = viewModel::refresh,
        onToggleGenre = viewModel::toggleGenre,
        onClearGenres = viewModel::clearGenreFilter,
        onUpdateSort = viewModel::updateSort,
    )
}

/**
 * Stateless screen body — extracted so `@Preview`s can render every UI state without standing
 * up a ViewModel/Hilt graph.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoviesListingScaffold(
    state: MoviesListingUiState,
    onMovieClick: (Long) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onToggleGenre: (Int) -> Unit,
    onClearGenres: () -> Unit,
    onUpdateSort: (SortCriterion, SortDirection) -> Unit,
) {
    var showSortSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.movies_listing_title)) },
                actions = {
                    IconButton(onClick = { showSortSheet = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.movies_listing_sort_action),
                        )
                    }
                    IconButton(
                        onClick = onRefresh,
                        enabled = !state.isLoading && !state.isRefreshing,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.movies_listing_refresh_action),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && !state.hasContent -> LoadingState()
                state.error != null && !state.hasContent -> ErrorState(
                    icon = Icons.Outlined.ErrorOutline,
                    title = stringResource(R.string.movies_listing_error_title),
                    message = state.error.toMessage(),
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
            EmptyFilterState(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacers.large),
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(MaterialTheme.spacers.medium),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacers.medium),
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
internal fun GenreFilterRow(
    genres: List<Genre>,
    selected: Set<Int>,
    onToggle: (Int) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (genres.isEmpty()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(
                horizontal = MaterialTheme.spacers.medium,
                vertical = MaterialTheme.spacers.small,
            ),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacers.small),
    ) {
        AssistChip(
            onClick = onClear,
            label = {
                Text(
                    if (selected.isEmpty()) stringResource(R.string.movies_listing_genre_chip_all)
                    else stringResource(R.string.movies_listing_genre_chip_clear)
                )
            },
        )
        genres.forEach { genre ->
            FilterChip(
                selected = genre.id in selected,
                onClick = { onToggle(genre.id) },
                label = { Text(genre.name) },
            )
        }
    }
}

@Composable
internal fun MovieListItem(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacers.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 84.dp, height = 120.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Poster(url = movie.posterUrl)
            }

            Column(
                modifier = Modifier
                    .padding(start = MaterialTheme.spacers.medium)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacers.extraSmall),
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (movie.genres.isNotEmpty()) {
                    Text(
                        text = movie.genres.joinToString { it.name },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = stringResource(R.string.movies_listing_rating_format, movie.voteAverage),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * Renders the movie poster.
 *
 * In Compose's "inspection mode" — i.e. inside the Studio preview pane and inside Roborazzi
 * screenshot tests driven by `ComposablePreviewScanner` — there is no real Coil [ImageLoader]
 * available, so [SubcomposeAsyncImage] would render an empty box. Instead we draw the same
 * [PosterPlaceholder] used as the production loading/error fallback, so previews and screenshot
 * goldens convey the full row layout (poster slot + text column) at a glance.
 *
 * In production (`LocalInspectionMode = false`) this is just a thin wrapper over
 * [SubcomposeAsyncImage] with the same behavior as before (placeholder while loading and on
 * error, real bitmap once the request resolves).
 */
@Composable
private fun Poster(url: String?) {
    if (url == null || LocalInspectionMode.current) {
        PosterPlaceholder()
    } else {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(120.dp),
            error = { PosterPlaceholder() },
            loading = { PosterPlaceholder() },
        )
    }
}

@Composable
private fun PosterPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Movie,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.6f),
            modifier = Modifier.size(32.dp),
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SortOptionsSheet(
    current: SortOption,
    onDismiss: () -> Unit,
    onApply: (SortCriterion, SortDirection) -> Unit,
) {
    var selectedCriterion by remember(current) { mutableStateOf(current.criterion) }
    var selectedDirection by remember(current) { mutableStateOf(current.direction) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(MaterialTheme.spacers.large)) {
            Text(
                text = stringResource(R.string.movies_listing_sort_sheet_title),
                style = MaterialTheme.typography.titleLarge,
            )
            SortCriterion.entries.forEach { criterion ->
                SelectableRow(
                    label = criterion.label(),
                    selected = selectedCriterion == criterion,
                    onClick = { selectedCriterion = criterion },
                )
            }

            Text(
                text = stringResource(R.string.movies_listing_sort_sheet_direction_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = MaterialTheme.spacers.medium),
            )
            SortDirection.entries.forEach { direction ->
                SelectableRow(
                    label = direction.label(),
                    selected = selectedDirection == direction,
                    onClick = { selectedDirection = direction },
                )
            }

            Button(
                onClick = { onApply(selectedCriterion, selectedDirection) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.spacers.large),
            ) { Text(stringResource(R.string.movies_listing_sort_sheet_apply)) }
        }
    }
}

@Composable
private fun SelectableRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = MaterialTheme.spacers.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacers.small),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

/**
 * `@Composable` mapper from a domain enum to a localised string, mirroring the
 * `MovieStatus.label()` pattern in `ui-detail` so the domain layer stays Android-resource-free.
 */
@Composable
private fun SortCriterion.label(): String = stringResource(
    when (this) {
        SortCriterion.Popularity -> R.string.sort_criterion_popularity
        SortCriterion.Title -> R.string.sort_criterion_title
        SortCriterion.ReleaseDate -> R.string.sort_criterion_release_date
    }
)

@Composable
private fun SortDirection.label(): String = stringResource(
    when (this) {
        SortDirection.Ascending -> R.string.sort_direction_ascending
        SortDirection.Descending -> R.string.sort_direction_descending
    }
)

@Composable
private fun EmptyFilterState(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.movies_listing_empty_filter_title),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = stringResource(R.string.movies_listing_empty_filter_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Maps a [DomainError] to a user-facing, localised string.
 *
 * Lives in the screen (not the ViewModel) so each variant can resolve via [stringResource],
 * which keeps the messages translatable and the ViewModel free of Android `Context` dependencies.
 */
@Composable
private fun DomainError.toMessage(): String = when (this) {
    is DomainError.Network -> stringResource(R.string.movies_listing_error_network)
    is DomainError.Server -> stringResource(R.string.movies_listing_error_server, code)
    is DomainError.Parsing -> stringResource(R.string.movies_listing_error_parsing)
    is DomainError.Cancelled -> stringResource(R.string.movies_listing_error_cancelled)
    is DomainError.Unknown -> stringResource(R.string.movies_listing_error_unknown)
}

// region Previews
//
@Composable
private fun MoviesListingPreview(state: MoviesListingUiState) = PreviewSurface {
    // Pin `LocalInspectionMode` to true so the Roborazzi screenshot tests (driven by
    // ComposablePreviewScanner) hit the same inspection-mode branches as the IDE preview pane —
    // notably the poster placeholder in `MovieListItem`.
    CompositionLocalProvider(LocalInspectionMode provides true) {
        MoviesListingScaffold(
            state = state,
            onMovieClick = {},
            onRetry = {},
            onRefresh = {},
            onToggleGenre = {},
            onClearGenres = {},
            onUpdateSort = { _, _ -> },
        )
    }
}

@PreviewLightDark
@Composable
private fun MoviesListingScreenLoadingPreview() =
    MoviesListingPreview(state = MoviesListingUiState(isLoading = true))

@PreviewLightDark
@Composable
private fun MoviesListingScreenErrorPreview() =
    MoviesListingPreview(state = MoviesListingUiState(error = DomainError.Network()))

@PreviewLightDark
@Composable
private fun MoviesListingScreenContentFullPreview() =
    MoviesListingPreview(state = previewListingState())

@PreviewLightDark
@Composable
private fun MoviesListingScreenContentEmptyFilterPreview() = MoviesListingPreview(
    // Movies are loaded but the active genre filter excludes every one of them — exercises
    // the "no movies match the selected genres" empty state above the chip row.
    state = previewListingState().let { full ->
        full.copy(
            selectedGenreIds = setOf(UNUSED_GENRE_ID),
            visibleMovies = emptyList(),
        )
    },
)

/**
 * Shared `MoviesListingUiState` fixture for previews. Defaults represent a fully-loaded screen
 * (3 movies, 3 derived genre chips, default sort). Each parameter can be overridden to exercise
 * the empty / loading / error paths.
 *
 * Lives alongside the previews (not in a separate `previews/` package) because nothing else in
 * the module — production or tests — uses it.
 */
private fun previewListingState(
    movies: List<Movie> = previewMovies(),
    selectedGenreIds: Set<Int> = emptySet(),
    sortOption: SortOption = SortOption.Default,
): MoviesListingUiState {
    val genres = movies.flatMap { it.genres }.distinctBy { it.id }.sortedBy { it.name }
    val visible = if (selectedGenreIds.isEmpty()) movies
    else movies.filter { m -> m.genreIds.any { it in selectedGenreIds } }
    return MoviesListingUiState(
        allMovies = movies,
        visibleMovies = visible,
        availableGenres = genres,
        selectedGenreIds = selectedGenreIds,
        sortOption = sortOption,
    )
}

private val GENRE_ACTION = Genre(id = 28, name = "Action")
private val GENRE_SCI_FI = Genre(id = 878, name = "Sci-Fi")
private val GENRE_DRAMA = Genre(id = 18, name = "Drama")
private val GENRE_THRILLER = Genre(id = 53, name = "Thriller")

/** Genre id deliberately not used by any preview movie, to drive the empty-filter state. */
private const val UNUSED_GENRE_ID: Int = 99_999

private fun previewMovies(): List<Movie> = listOf(
    Movie(
        id = 27_205L,
        title = "Inception",
        posterUrl = "https://image.tmdb.org/t/p/w500/inception.jpg",
        backdropUrl = null,
        genreIds = listOf(GENRE_ACTION.id, GENRE_SCI_FI.id, GENRE_THRILLER.id),
        genres = listOf(GENRE_ACTION, GENRE_SCI_FI, GENRE_THRILLER),
        popularity = 88.0,
        releaseDate = LocalDate.of(2010, 7, 16),
        voteAverage = 8.4,
    ),
    Movie(
        id = 157_336L,
        title = "Interstellar",
        posterUrl = "https://image.tmdb.org/t/p/w500/interstellar.jpg",
        backdropUrl = null,
        genreIds = listOf(GENRE_DRAMA.id, GENRE_SCI_FI.id),
        genres = listOf(GENRE_DRAMA, GENRE_SCI_FI),
        popularity = 92.5,
        releaseDate = LocalDate.of(2014, 11, 7),
        voteAverage = 8.6,
    ),
    Movie(
        id = 603L,
        title = "The Matrix",
        posterUrl = null,
        backdropUrl = null,
        genreIds = listOf(GENRE_ACTION.id, GENRE_SCI_FI.id),
        genres = listOf(GENRE_ACTION, GENRE_SCI_FI),
        popularity = 75.1,
        releaseDate = LocalDate.of(1999, 3, 31),
        voteAverage = 8.2,
    ),
)
// endregion
