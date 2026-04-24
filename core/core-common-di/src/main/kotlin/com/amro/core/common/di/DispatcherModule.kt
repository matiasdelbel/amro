package com.amro.core.common.di

import com.amro.core.common.dispatcher.DefaultDispatcherProvider
import com.amro.core.common.dispatcher.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt binding for [DispatcherProvider].
 *
 * Lives in its own tiny module (rather than inside `core-common` or any feature data module)
 * so that:
 *  - `core-common` stays a pure Kotlin/JVM module — portable, fast to compile, no Android/Hilt
 *    plumbing — and consumers without DI can still construct [DefaultDispatcherProvider]
 *    directly.
 *  - The binding's ownership matches its abstraction's ownership: anyone who depends on
 *    `core-common-di` automatically gets the implementation, regardless of whether other
 *    feature modules (e.g. `:features:movies:data`) are on the classpath.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object DispatcherModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
}
