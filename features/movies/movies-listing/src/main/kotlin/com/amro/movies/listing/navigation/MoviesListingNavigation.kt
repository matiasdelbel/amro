package com.amro.movies.listing.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.amro.movies.listing.MoviesListingRoute

/**
 * Navigation route literal owned by the listing module. Keeping it here (rather than in the app
 * module) means any future feature that needs to deep-link to the trending list can depend on
 * this module alone.
 */
const val MOVIES_LISTING_ROUTE: String = "movies/listing"

fun NavGraphBuilder.moviesListingDestination(
    onMovieClick: (movieId: Long) -> Unit,
) {
    composable(route = MOVIES_LISTING_ROUTE) {
        MoviesListingRoute(onMovieClick = onMovieClick)
    }
}
