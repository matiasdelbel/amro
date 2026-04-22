package com.amro.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.amro.designsystem.preview.PreviewSurface
import com.amro.designsystem.preview.ThemePreviews

/**
 * Visual reference for the live color scheme. Useful to eyeball both schemes whenever we
 * touch [Color.kt] so unintended contrast regressions get caught in review.
 */
@ThemePreviews
@Composable
private fun AmroThemeSwatchesPreview() {
    PreviewSurface {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Swatch("Primary", MaterialTheme.colorScheme.primary)
                Swatch("Secondary", MaterialTheme.colorScheme.secondary)
                Swatch("Tertiary", MaterialTheme.colorScheme.tertiary)
                Swatch("Error", MaterialTheme.colorScheme.error)
            }
            Text(
                text = "The quick brown fox jumps over the lazy dog",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                text = "Small supporting text in onSurfaceVariant",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Swatch(label: String, color: Color) {
    Column {
        Box(modifier = Modifier.size(56.dp).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
