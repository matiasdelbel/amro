package com.amro.movies.data.di

import com.amro.movies.data.MoviesRepositoryImpl
import com.amro.movies.domain.repository.MoviesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Binds the data-layer repository implementation to the domain-layer contract.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class MoviesDataModule {

    @Binds
    abstract fun bindMoviesRepository(impl: MoviesRepositoryImpl): MoviesRepository
}
