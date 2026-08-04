package com.musicplayer.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * All singletons in this app are constructor-injected and annotated with
 * `@Singleton`, so Hilt can build them without explicit `@Provides` methods.
 * The module is kept as the anchor point for any future binding that does
 * need manual construction.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
