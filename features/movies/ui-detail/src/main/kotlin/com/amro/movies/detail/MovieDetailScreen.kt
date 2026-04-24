package com.amro.movies.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.amro.movies.domain.MovieDetail
import com.amro.movies.domain.MovieStatus
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MovieDetailScreen(
    onBack: () -> Unit,
    onOpenImdb: (url: String) -> Unit,
    viewModel: MovieDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MovieDetailScaffold(
        state = state,
        onBack = onBack,
        onRetry = viewModel::retry,
        onOpenImdb = onOpenImdb,
    )
}

/**
 * Stateless screen body — extracted so `@Preview`s can render every UI state without standing
 * up a ViewModel/Hilt graph.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieDetailScaffold(
    state: MovieDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenImdb: (url: String) -> Unit,
) {
    val fallbackTitle = stringResource(R.string.movie_detail_default_title)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text((state as? MovieDetailUiState.Content)?.movie?.title ?: fallbackTitle)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.movie_detail_back),
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (state) {
                MovieDetailUiState.Loading -> LoadingState()
                is MovieDetailUiState.Error -> ErrorState(
                    icon = Icons.Outlined.ErrorOutline,
                    title = stringResource(R.string.movie_detail_error_title),
                    message = state.error.toMessage(),
                    onRetry = onRetry,
                )
                is MovieDetailUiState.Content -> MovieDetailContent(
                    movie = state.movie,
                    onOpenImdb = onOpenImdb,
                )
            }
        }
    }
}

@Composable
private fun MovieDetailContent(
    movie: MovieDetail,
    onOpenImdb: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = MaterialTheme.spacers.large),
    ) {
        val hero = movie.backdropUrl ?: movie.posterUrl
        if (hero != null) {
            HeroImage(
                url = hero,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
            )
        }

        Column(
            modifier = Modifier.padding(all = MaterialTheme.spacers.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacers.medium),
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            movie.tagline?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Light,
                )
            }

            if (movie.genres.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacers.small),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    movie.genres.take(4).forEach { g ->
                        AssistChip(onClick = {}, label = { Text(g.name) })
                    }
                }
            }

            VoteRow(movie)

            movie.overview?.let {
                Text(
                    text = stringResource(R.string.movie_detail_overview),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }

            FactsCard(movie)

            movie.imdbUrl?.let { url ->
                FilledTonalButton(
                    onClick = { onOpenImdb(url) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                    Text(
                        text = stringResource(R.string.movie_detail_open_imdb),
                        modifier = Modifier.padding(start = MaterialTheme.spacers.small),
                    )
                }
            }
        }
    }
}

@Composable
private fun VoteRow(movie: MovieDetail) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacers.medium),
    ) {
        Column {
            Text(
                text = stringResource(R.string.movie_detail_rating_format, movie.voteAverage),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = pluralStringResource(
                    R.plurals.movie_detail_votes,
                    movie.voteCount,
                    movie.voteCount,
                ),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        movie.runtimeMinutes?.let {
            Column {
                Text(
                    text = stringResource(R.string.movie_detail_runtime_minutes, it),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.movie_detail_runtime_label),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        movie.releaseDate?.let {
            Column {
                Text(
                    text = it.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.movie_detail_release_label),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun FactsCard(movie: MovieDetail) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacers.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacers.small),
        ) {
            Fact(stringResource(R.string.movie_detail_status_label), movie.status.label())
            Fact(stringResource(R.string.movie_detail_budget_label), formatMoney(movie.budget))
            Fact(stringResource(R.string.movie_detail_revenue_label), formatMoney(movie.revenue))
        }
    }
}

/**
 * Renders the movie backdrop / poster.
 *
 * In Compose's "inspection mode" — i.e. inside the Studio preview pane and inside Roborazzi
 * screenshot tests driven by `ComposablePreviewScanner` — there is no real Coil [ImageLoader]
 * available, so [SubcomposeAsyncImage] would render an empty 16:9 area. Instead we draw a
 * branded gradient + movie-icon placeholder, so previews and screenshot goldens convey the
 * full layout (hero area + everything below) at a glance.
 *
 * In production (`LocalInspectionMode = false`) this is just a thin wrapper over
 * [SubcomposeAsyncImage] with the same behavior as before.
 */
@Composable
private fun HeroImage(url: String, modifier: Modifier = Modifier) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary,
                    ),
                ),
            ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Movie,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(72.dp),
            )
        }
    } else {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    }
}

@Composable
private fun Fact(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End)
    }
}

/**
 * Mirrors the [DomainError.toMessage] pattern: a `@Composable` mapper from a domain enum to a
 * localised string, so the domain layer stays free of Android resources.
 */
@Composable
private fun MovieStatus.label(): String = stringResource(
    when (this) {
        MovieStatus.Rumored -> R.string.movie_status_rumored
        MovieStatus.Planned -> R.string.movie_status_planned
        MovieStatus.InProduction -> R.string.movie_status_in_production
        MovieStatus.PostProduction -> R.string.movie_status_post_production
        MovieStatus.Released -> R.string.movie_status_released
        MovieStatus.Canceled -> R.string.movie_status_canceled
        MovieStatus.Unknown -> R.string.movie_status_unknown
    }
)

private fun formatMoney(value: Long): String {
    if (value <= 0L) return "—"
    val fmt = NumberFormat.getCurrencyInstance(Locale.US)
    fmt.maximumFractionDigits = 0
    return fmt.format(value)
}

/**
 * Maps a [DomainError] to a user-facing, localised string.
 *
 * Lives in the screen (not the ViewModel) so each variant can resolve via [stringResource],
 * which keeps the messages translatable and the ViewModel free of Android `Context` dependencies.
 */
@Composable
private fun DomainError.toMessage(): String = when (this) {
    is DomainError.Network -> stringResource(R.string.movie_detail_error_network)
    is DomainError.Server -> stringResource(R.string.movie_detail_error_server, code)
    is DomainError.Parsing -> stringResource(R.string.movie_detail_error_parsing)
    is DomainError.Cancelled -> stringResource(R.string.movie_detail_error_cancelled)
    is DomainError.Unknown -> stringResource(R.string.movie_detail_error_unknown)
}

// region Previews
//
@Composable
private fun MovieDetailPreview(state: MovieDetailUiState) = PreviewSurface {
    CompositionLocalProvider(LocalInspectionMode provides true) {
        MovieDetailScaffold(
            state = state,
            onBack = {},
            onRetry = {},
            onOpenImdb = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun MovieDetailScreenLoadingPreview() =
    MovieDetailPreview(state = MovieDetailUiState.Loading)

@PreviewLightDark
@Composable
private fun MovieDetailScreenErrorPreview() =
    MovieDetailPreview(state = MovieDetailUiState.Error(DomainError.Network()))

@PreviewLightDark
@Composable
private fun MovieDetailScreenContentFullPreview() =
    MovieDetailPreview(state = MovieDetailUiState.Content(previewMovieDetail()))

@PreviewLightDark
@Composable
private fun MovieDetailScreenContentMinimalPreview() = MovieDetailPreview(
    state = MovieDetailUiState.Content(
        previewMovieDetail(
            tagline = null,
            posterUrl = null,
            backdropUrl = null,
            genres = emptyList(),
            overview = null,
            voteCount = 1,
            budget = 0,
            revenue = 0,
            imdbUrl = null,
            runtimeMinutes = null,
            releaseDate = null,
            status = MovieStatus.Planned,
        ),
    ),
)

/**
 * Shared `MovieDetail` fixture for previews. Defaults represent a "fully populated" movie;
 * each parameter can be overridden to exercise the nullable / empty paths.
 *
 * Lives alongside the previews (not in a separate `previews/` package) because nothing else
 * in the module — production or tests — uses it.
 */
private fun previewMovieDetail(
    id: Long = 27_205L,
    title: String = "Inception",
    tagline: String? = "Your mind is the scene of the crime.",
    posterUrl: String? = "https://image.tmdb.org/t/p/w500/poster.jpg",
    backdropUrl: String? = "https://image.tmdb.org/t/p/w1280/backdrop.jpg",
    genres: List<Genre> = listOf(
        Genre(id = 28, name = "Action"),
        Genre(id = 878, name = "Sci-Fi"),
        Genre(id = 53, name = "Thriller"),
    ),
    overview: String? = "A skilled thief is given a chance at redemption if he can successfully " +
        "perform an inception: planting an idea in someone's subconscious.",
    voteAverage: Double = 8.4,
    voteCount: Int = 36_421,
    budget: Long = 160_000_000L,
    revenue: Long = 836_800_000L,
    status: MovieStatus = MovieStatus.Released,
    imdbUrl: String? = "https://www.imdb.com/title/tt1375666",
    runtimeMinutes: Int? = 148,
    releaseDate: LocalDate? = LocalDate.of(2010, 7, 16),
    homepage: String? = null,
) = MovieDetail(
    id = id,
    title = title,
    tagline = tagline,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    genres = genres,
    overview = overview,
    voteAverage = voteAverage,
    voteCount = voteCount,
    budget = budget,
    revenue = revenue,
    status = status,
    imdbUrl = imdbUrl,
    runtimeMinutes = runtimeMinutes,
    releaseDate = releaseDate,
    homepage = homepage,
)
// endregion
