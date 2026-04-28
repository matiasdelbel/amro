package com.amro.movies

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.amro.movies.detail.movieDetailDestination
import com.amro.movies.detail.movieDetailPath
import com.amro.movies.listing.MOVIES_LISTING_ROUTE
import com.amro.movies.listing.moviesListingDestination

/**
 * Top-level Jetpack Compose navigation graph.
 */
@Composable
fun AmroNavHost(
    onOpenExternalUrl: (String) -> Unit,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MOVIES_LISTING_ROUTE,
    ) {
        moviesListingDestination(
            onMovieClick = { movieId -> navController.navigate(route = movieDetailPath(movieId)) },
        )
        movieDetailDestination(
            onBack = { navController.popBackStack() },
            onOpenImdb = onOpenExternalUrl,
        )
    }
}
