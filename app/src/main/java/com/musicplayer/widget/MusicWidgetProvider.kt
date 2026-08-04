package com.musicplayer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.musicplayer.MainActivity
import com.musicplayer.R

/**
 * Home screen widget with transport controls.
 *
 * The widget renders from a small cache of the last known playback state so
 * it still shows something useful when the app process has been killed.
 */
class MusicWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, buildViews(context))
        }
    }

    companion object {
        private const val PREFS = "music_widget_state"
        private const val KEY_TITLE = "title"
        private const val KEY_ARTIST = "artist"
        private const val KEY_PLAYING = "playing"

        /** Called from the playback service whenever the state changes. */
        fun pushUpdate(context: Context, title: String?, artist: String?, isPlaying: Boolean) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_TITLE, title)
                .putString(KEY_ARTIST, artist)
                .putBoolean(KEY_PLAYING, isPlaying)
                .apply()

            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, MusicWidgetProvider::class.java)
            )
            if (ids.isEmpty()) return
            val views = buildViews(context)
            ids.forEach { manager.updateAppWidget(it, views) }
        }

        private fun buildViews(context: Context): RemoteViews {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val title = prefs.getString(KEY_TITLE, null)
            val artist = prefs.getString(KEY_ARTIST, null)
            val isPlaying = prefs.getBoolean(KEY_PLAYING, false)

            return RemoteViews(context.packageName, R.layout.widget_music_player).apply {
                setTextViewText(
                    R.id.widget_title,
                    title ?: context.getString(R.string.widget_nothing_playing)
                )
                setTextViewText(
                    R.id.widget_artist,
                    artist ?: context.getString(R.string.widget_tap_to_open)
                )
                setImageViewResource(
                    R.id.widget_play_pause,
                    if (isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
                )

                setOnClickPendingIntent(R.id.widget_root, openAppIntent(context))
                setOnClickPendingIntent(
                    R.id.widget_previous,
                    actionIntent(context, WidgetActionReceiver.ACTION_PREVIOUS, 1)
                )
                setOnClickPendingIntent(
                    R.id.widget_play_pause,
                    actionIntent(context, WidgetActionReceiver.ACTION_TOGGLE, 2)
                )
                setOnClickPendingIntent(
                    R.id.widget_next,
                    actionIntent(context, WidgetActionReceiver.ACTION_NEXT, 3)
                )
            }
        }

        private fun openAppIntent(context: Context): PendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        private fun actionIntent(
            context: Context,
            action: String,
            requestCode: Int
        ): PendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, WidgetActionReceiver::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
