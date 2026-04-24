package com.amro.movies.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.amro.movies.detail.movieDetailDestination
import com.amro.movies.detail.movieDetailPath
import com.amro.movies.listing.navigation.MOVIES_LISTING_ROUTE
import com.amro.movies.listing.navigation.moviesListingDestination

/**
 * Top-level Jetpack Compose navigation graph.
 *
 * The app module owns this file — it's the only place that knows about **both** features, so it
 * is also the right place to stitch navigation actions (listing → detail) together. Each feature
 * module only exposes its own route literal + `NavGraphBuilder` extension.
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
            onMovieClick = { movieId ->
                navController.navigate(movieDetailPath(movieId))
            },
        )
        movieDetailDestination(
            onBack = { navController.popBackStack() },
            onOpenImdb = onOpenExternalUrl,
        )
    }
}
