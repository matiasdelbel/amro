package com.amro.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Light scheme
internal val Primary = Color(0xFF6750A4)
internal val OnPrimary = Color(0xFFFFFFFF)
internal val PrimaryContainer = Color(0xFFEADDFF)
internal val OnPrimaryContainer = Color(0xFF21005D)
internal val Secondary = Color(0xFF625B71)
internal val OnSecondary = Color(0xFFFFFFFF)
internal val SecondaryContainer = Color(0xFFE8DEF8)
internal val OnSecondaryContainer = Color(0xFF1D192B)
internal val Tertiary = Color(0xFF7D5260)
internal val Background = Color(0xFFFFFBFE)
internal val OnBackground = Color(0xFF1C1B1F)
internal val Surface = Color(0xFFFFFBFE)
internal val OnSurface = Color(0xFF1C1B1F)
internal val Error = Color(0xFFB3261E)
internal val OnError = Color(0xFFFFFFFF)

// Dark scheme
internal val PrimaryDark = Color(0xFFD0BCFF)
internal val OnPrimaryDark = Color(0xFF381E72)
internal val PrimaryContainerDark = Color(0xFF4F378B)
internal val OnPrimaryContainerDark = Color(0xFFEADDFF)
internal val SecondaryDark = Color(0xFFCCC2DC)
internal val OnSecondaryDark = Color(0xFF332D41)
internal val BackgroundDark = Color(0xFF1C1B1F)
internal val OnBackgroundDark = Color(0xFFE6E1E5)
internal val SurfaceDark = Color(0xFF1C1B1F)
internal val OnSurfaceDark = Color(0xFFE6E1E5)

internal val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    error = Error,
    onError = OnError,
)

internal val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
)
