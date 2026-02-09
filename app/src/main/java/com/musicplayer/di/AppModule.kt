package com.musicplayer.di

import android.content.Context
import com.musicplayer.data.repository.MediaRepository
import com.musicplayer.data.repository.PlaylistRepository
import com.musicplayer.player.PlaybackController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMediaRepository(
        @ApplicationContext context: Context
    ): MediaRepository {
        return MediaRepository(context)
    }

    @Provides
    @Singleton
    fun providePlaybackController(
        @ApplicationContext context: Context
    ): PlaybackController {
        return PlaybackController(context)
    }

    @Provides
    @Singleton
    fun providePlaylistRepository(
        @ApplicationContext context: Context
    ): PlaylistRepository {
        return PlaylistRepository(context)
    }
}
