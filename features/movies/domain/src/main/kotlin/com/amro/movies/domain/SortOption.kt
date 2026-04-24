package com.amro.movies.domain

data class SortOption(
    val criterion: SortCriterion = SortCriterion.Popularity,
    val direction: SortDirection = SortDirection.Descending,
) {
    companion object {
        /** Popularity descending, as required by the MVP spec. */
        val Default = SortOption(criterion = SortCriterion.Popularity, direction = SortDirection.Descending)
    }
}

enum class SortCriterion { Popularity, Title, ReleaseDate }

enum class SortDirection { Ascending, Descending }
