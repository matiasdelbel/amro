package com.amro.movies.listing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.amro.movies.domain.usecase.SortCriterion
import com.amro.movies.domain.usecase.SortDirection
import com.amro.movies.domain.usecase.SortOption

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
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Sort by", style = MaterialTheme.typography.titleLarge)
            SortCriterion.entries.forEach { criterion ->
                SelectableRow(
                    label = criterion.toLabel(),
                    selected = selectedCriterion == criterion,
                    onClick = { selectedCriterion = criterion },
                )
            }

            Text(
                "Direction",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp),
            )
            SortDirection.entries.forEach { direction ->
                SelectableRow(
                    label = direction.toLabel(),
                    selected = selectedDirection == direction,
                    onClick = { selectedDirection = direction },
                )
            }

            Button(
                onClick = { onApply(selectedCriterion, selectedDirection) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
            ) { Text("Apply") }
        }
    }
}

@Composable
private fun SelectableRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun SortCriterion.toLabel(): String = when (this) {
    SortCriterion.Popularity -> "Popularity"
    SortCriterion.Title -> "Title"
    SortCriterion.ReleaseDate -> "Release date"
}

private fun SortDirection.toLabel(): String = when (this) {
    SortDirection.Ascending -> "Ascending"
    SortDirection.Descending -> "Descending"
}
