package com.amro.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Convenience wrapper that applies [AmroTheme] and a Material3 [Surface] so previews pick up
 * the right background color and `onSurface` text styling automatically.
 */
@Composable
fun PreviewSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AmroTheme {
        Surface(
            modifier = modifier.background(color = MaterialTheme.colorScheme.background),
            content = content,
        )
    }
}
