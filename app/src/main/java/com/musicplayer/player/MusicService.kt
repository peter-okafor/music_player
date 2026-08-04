package com.musicplayer.player

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.musicplayer.MainActivity
import com.musicplayer.R
import com.musicplayer.widget.MusicWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaSessionService() {

    @Inject
    lateinit var audioEffects: AudioEffectsController

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                /* handleAudioFocus = */ true
            )
            .setHandleAudioBecomingNoisy(true)
            .setSeekBackIncrementMs(SEEK_INCREMENT_MS)
            .setSeekForwardIncrementMs(SEEK_INCREMENT_MS)
            .build()
        player = exoPlayer

        // Bind the equaliser chain to this player's audio session.
        audioEffects.attachSession(exoPlayer.audioSessionId)
        exoPlayer.addAnalyticsListener(object : AnalyticsListener {
            override fun onAudioSessionIdChanged(
                eventTime: AnalyticsListener.EventTime,
                audioSessionId: Int
            ) {
                audioEffects.attachSession(audioSessionId)
            }
        })

        // Keep the home screen widget in step with playback.
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) = pushWidgetUpdate()

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) =
                pushWidgetUpdate()

            override fun onPlaybackStateChanged(playbackState: Int) = pushWidgetUpdate()
        })

        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setChannelName(R.string.playback_channel_name)
                .setNotificationId(NOTIFICATION_ID)
                .build()
                .apply { setSmallIcon(R.drawable.ic_notification) }
        )

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(sessionActivity)
            .setCallback(MediaSessionCallback())
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val activePlayer = mediaSession?.player
        if (activePlayer == null || !activePlayer.playWhenReady || activePlayer.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        audioEffects.release()
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        player = null
        MusicWidgetProvider.pushUpdate(this, title = null, artist = null, isPlaying = false)
        super.onDestroy()
    }

    private fun pushWidgetUpdate() {
        val current = player ?: return
        MusicWidgetProvider.pushUpdate(
            context = this,
            title = current.mediaMetadata.title?.toString(),
            artist = current.mediaMetadata.artist?.toString(),
            isPlaying = current.isPlaying
        )
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val resolved = mediaItems.map { item ->
                item.buildUpon()
                    .setUri(item.requestMetadata.mediaUri ?: item.localConfiguration?.uri)
                    .build()
            }.toMutableList()
            return Futures.immediateFuture(resolved)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_CHANNEL_ID = "music_playback"
        private const val SEEK_INCREMENT_MS = 10_000L
    }
}
