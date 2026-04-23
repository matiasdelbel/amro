package com.amro.movies.detail.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.amro.movies.detail.MovieDetailRoute

internal const val MOVIE_ID_ARG = "movieId"
private const val MOVIES_DETAIL_BASE = "movies/detail"
const val MOVIES_DETAIL_ROUTE: String = "$MOVIES_DETAIL_BASE/{$MOVIE_ID_ARG}"

fun movieDetailPath(movieId: Long): String = "$MOVIES_DETAIL_BASE/$movieId"

/**
 * Type-safe wrapper around the single `movieId` navigation argument. Using a dedicated class
 * keeps SavedStateHandle usage encapsulated and gives us a single place to fail fast if the
 * arg is missing – the ViewModel wouldn't know what to do without it.
 */
class MovieDetailArgs(val movieId: Long) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        movieId = requireNotNull(savedStateHandle.get<Long>(MOVIE_ID_ARG)) {
            "movieId argument is missing for MovieDetail destination"
        }
    )
}

fun NavGraphBuilder.movieDetailDestination(
    onBack: () -> Unit,
    onOpenImdb: (url: String) -> Unit,
) {
    composable(
        route = MOVIES_DETAIL_ROUTE,
        arguments = listOf(
            navArgument(MOVIE_ID_ARG) {
                type = NavType.LongType
            }
        ),
    ) {
        MovieDetailRoute(onBack = onBack, onOpenImdb = onOpenImdb)
    }
}
