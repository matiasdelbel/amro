package com.amro.core.coroutine.di

import com.amro.core.coroutine.dispatcher.DefaultDispatcherProvider
import com.amro.core.coroutine.dispatcher.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt binding for [DispatcherProvider].
 *
 * Lives next to the abstraction itself: anyone depending on `:core:coroutine` gets both
 * the contract and the binding in a single module, so feature modules don't have to
 * choreograph two separate "API + DI" dependencies.
 *
 * Consumers without DI can still construct [DefaultDispatcherProvider] directly.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object DispatcherModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
}
