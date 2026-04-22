package com.amro.movies.data.di

import com.amro.core.common.dispatcher.DefaultDispatcherProvider
import com.amro.core.common.dispatcher.DispatcherProvider
import com.amro.movies.data.repository.MoviesRepositoryImpl
import com.amro.movies.domain.repository.MoviesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class MoviesDataBindings {
    @Binds
    @Singleton
    abstract fun bindMoviesRepository(impl: MoviesRepositoryImpl): MoviesRepository
}

@Module
@InstallIn(SingletonComponent::class)
internal object MoviesDataProviders {
    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
}
