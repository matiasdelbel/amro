package com.amro.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun AmroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    spacers: Spacers = DefaultSpacers,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    CompositionLocalProvider(
        LocalSpacers provides spacers,
    ) {
        MaterialTheme(colorScheme = colors, content = content)
    }
}
