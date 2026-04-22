package com.amro.designsystem.preview

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.amro.designsystem.theme.AmroTheme

/**
 * Multi-preview annotation that renders a composable in both the light and dark Material3
 * schemes of [AmroTheme]. Apply this to any preview function instead of writing two `@Preview`
 * declarations by hand.
 *
 * Usage:
 * ```
 * @ThemePreviews
 * @Composable
 * private fun MyComponentPreview() {
 *     PreviewSurface { MyComponent(...) }
 * }
 * ```
 */
@Preview(name = "Light", group = "theme", showBackground = true)
@Preview(
    name = "Dark",
    group = "theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
annotation class ThemePreviews

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
            modifier = modifier.background(MaterialTheme.colorScheme.background),
            content = content,
        )
    }
}
