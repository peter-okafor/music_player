package com.musicplayer.widget

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.musicplayer.player.MusicService

/**
 * Executes widget button presses.
 *
 * The widget can be tapped when the app process is not running, so this
 * connects to the media session on demand rather than assuming the in-app
 * controller exists.
 */
class WidgetActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action !in setOf(ACTION_TOGGLE, ACTION_NEXT, ACTION_PREVIOUS)) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        val token = SessionToken(appContext, ComponentName(appContext, MusicService::class.java))
        val future = MediaController.Builder(appContext, token).buildAsync()

        future.addListener({
            var controller: MediaController? = null
            try {
                controller = future.get()
                when (action) {
                    ACTION_TOGGLE -> if (controller.isPlaying) controller.pause() else controller.play()
                    ACTION_NEXT -> controller.seekToNextMediaItem()
                    ACTION_PREVIOUS -> controller.seekToPreviousMediaItem()
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Widget action failed: $action", e)
            } finally {
                controller?.release()
                pendingResult.finish()
            }
        }, MoreExecutors.directExecutor())
    }

    companion object {
        private const val TAG = "WidgetActionReceiver"
        const val ACTION_TOGGLE = "com.musicplayer.widget.TOGGLE"
        const val ACTION_NEXT = "com.musicplayer.widget.NEXT"
        const val ACTION_PREVIOUS = "com.musicplayer.widget.PREVIOUS"
    }
}
