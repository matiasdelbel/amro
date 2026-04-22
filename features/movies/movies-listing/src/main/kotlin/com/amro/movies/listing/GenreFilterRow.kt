package com.amro.movies.listing

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.amro.designsystem.theme.spacers
import com.amro.movies.domain.model.Genre

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
            label = { Text(if (selected.isEmpty()) "All" else "Clear") },
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
