package com.amro.movies.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.outlined.ErrorOutline
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.amro.designsystem.components.ErrorState
import com.amro.designsystem.components.LoadingState
import com.amro.designsystem.theme.spacers
import com.amro.movies.domain.MovieDetail
import com.amro.movies.domain.MovieStatus
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailRoute(
    onBack: () -> Unit,
    onOpenImdb: (url: String) -> Unit,
    viewModel: MovieDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text((state as? MovieDetailUiState.Content)?.movie?.title ?: "Movie") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                MovieDetailUiState.Loading -> LoadingState()
                is MovieDetailUiState.Error -> ErrorState(
                    icon = Icons.Outlined.ErrorOutline,
                    title = "Couldn't load movie",
                    message = s.message,
                    onRetry = viewModel::retry,
                )
                is MovieDetailUiState.Content -> MovieDetailContent(
                    movie = s.movie,
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
            SubcomposeAsyncImage(
                model = hero,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
            )
        }

        Column(
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacers.medium,
                vertical = MaterialTheme.spacers.medium,
            ),
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
                Text("Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
                        "Open on IMDb",
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
                "%.1f ★".format(movie.voteAverage),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text("${movie.voteCount} votes", style = MaterialTheme.typography.labelMedium)
        }
        movie.runtimeMinutes?.let {
            Column {
                Text("${it} min", style = MaterialTheme.typography.titleMedium)
                Text("Runtime", style = MaterialTheme.typography.labelMedium)
            }
        }
        movie.releaseDate?.let {
            Column {
                Text(
                    it.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text("Release", style = MaterialTheme.typography.labelMedium)
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
            Fact("Status", movie.status.label())
            Fact("Budget", formatMoney(movie.budget))
            Fact("Revenue", formatMoney(movie.revenue))
        }
    }
}

@Composable
private fun Fact(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End)
    }
}

private fun MovieStatus.label(): String = when (this) {
    MovieStatus.Rumored -> "Rumored"
    MovieStatus.Planned -> "Planned"
    MovieStatus.InProduction -> "In Production"
    MovieStatus.PostProduction -> "Post Production"
    MovieStatus.Released -> "Released"
    MovieStatus.Canceled -> "Canceled"
    MovieStatus.Unknown -> "Unknown"
}

private fun formatMoney(value: Long): String {
    if (value <= 0L) return "—"
    val fmt = NumberFormat.getCurrencyInstance(Locale.US)
    fmt.maximumFractionDigits = 0
    return fmt.format(value)
}
