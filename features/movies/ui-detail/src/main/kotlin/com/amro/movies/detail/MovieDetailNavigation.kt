package com.amro.movies.detail

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

fun NavGraphBuilder.movieDetailDestination(
    onBack: () -> Unit,
    onOpenImdb: (url: String) -> Unit,
) {
    composable(
        route = MOVIES_DETAIL_ROUTE,
        arguments = listOf(navArgument(name = MOVIE_ID_ARG) { type = NavType.LongType }),
    ) {
        MovieDetailScreen(onBack = onBack, onOpenImdb = onOpenImdb)
    }
}

fun movieDetailPath(movieId: Long): String = "$MOVIES_DETAIL_BASE/$movieId"

/**
 * Type-safe wrapper around the single `movieId` navigation argument. Using a dedicated class
 * keeps SavedStateHandle usage encapsulated and gives us a single place to fail fast if the
 * arg is missing – the ViewModel wouldn't know what to do without it.
 */
internal class MovieDetailArgs(val movieId: Long) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        movieId = requireNotNull(value = savedStateHandle.get<Long>(MOVIE_ID_ARG)) {
            "movieId argument is missing for MovieDetail destination"
        }
    )
}

internal const val MOVIE_ID_ARG = "movieId"
private const val MOVIES_DETAIL_BASE = "movies/detail"
private const val MOVIES_DETAIL_ROUTE: String = "$MOVIES_DETAIL_BASE/{$MOVIE_ID_ARG}"
