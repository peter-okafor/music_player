package com.musicplayer.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { SYSTEM, DARK, LIGHT }

/**
 * Small, synchronous key/value store for everything the user can change.
 *
 * SharedPreferences (rather than Room/DataStore) keeps the dependency
 * surface flat — none of this data is relational and all of it is tiny.
 * State is mirrored into StateFlows so Compose can observe it directly.
 */
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("music_player_prefs", Context.MODE_PRIVATE)

    // ---------------------------------------------------------------- theme

    private val _themeMode = MutableStateFlow(
        runCatching { ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.DARK.name)!!) }
            .getOrDefault(ThemeMode.DARK)
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit { putString(KEY_THEME, mode.name) }
        _themeMode.value = mode
    }

    private val _dynamicColor = MutableStateFlow(prefs.getBoolean(KEY_DYNAMIC_COLOR, true))
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    fun setDynamicColor(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_DYNAMIC_COLOR, enabled) }
        _dynamicColor.value = enabled
    }

    // ------------------------------------------------------------ favorites

    private val _favorites = MutableStateFlow(
        prefs.getStringSet(KEY_FAVORITES, emptySet())?.toSet() ?: emptySet()
    )
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    fun toggleFavorite(trackId: String): Boolean {
        val next = _favorites.value.toMutableSet()
        val added = if (next.contains(trackId)) {
            next.remove(trackId); false
        } else {
            next.add(trackId); true
        }
        prefs.edit { putStringSet(KEY_FAVORITES, next) }
        _favorites.value = next
        return added
    }

    fun isFavorite(trackId: String): Boolean = _favorites.value.contains(trackId)

    // -------------------------------------------------------- recently played

    private val _recentlyPlayed = MutableStateFlow(
        prefs.getString(KEY_RECENT, "")!!.split("|").filter { it.isNotBlank() }
    )
    val recentlyPlayed: StateFlow<List<String>> = _recentlyPlayed.asStateFlow()

    fun recordPlayed(trackId: String) {
        val next = (listOf(trackId) + _recentlyPlayed.value.filter { it != trackId })
            .take(MAX_RECENT)
        prefs.edit { putString(KEY_RECENT, next.joinToString("|")) }
        _recentlyPlayed.value = next

        val counts = playCounts().toMutableMap()
        counts[trackId] = (counts[trackId] ?: 0) + 1
        prefs.edit {
            putString(KEY_PLAY_COUNTS, counts.entries.joinToString("|") { "${it.key}:${it.value}" })
        }
    }

    fun playCounts(): Map<String, Int> =
        prefs.getString(KEY_PLAY_COUNTS, "")!!
            .split("|")
            .filter { it.contains(":") }
            .associate {
                val (id, count) = it.split(":", limit = 2)
                id to (count.toIntOrNull() ?: 0)
            }

    // ----------------------------------------------------------------- sort

    fun getSort(tab: String, default: String): String =
        prefs.getString("$KEY_SORT_PREFIX$tab", default) ?: default

    fun setSort(tab: String, value: String) {
        prefs.edit { putString("$KEY_SORT_PREFIX$tab", value) }
    }

    // ------------------------------------------------------------- playback

    private val _playbackSpeed = MutableStateFlow(prefs.getFloat(KEY_SPEED, 1f))
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    fun setPlaybackSpeed(speed: Float) {
        prefs.edit { putFloat(KEY_SPEED, speed) }
        _playbackSpeed.value = speed
    }

    var lastSleepMinutes: Int
        get() = prefs.getInt(KEY_SLEEP_MINUTES, 30)
        set(value) = prefs.edit { putInt(KEY_SLEEP_MINUTES, value) }

    // ------------------------------------------------------------ equalizer

    var equalizerEnabled: Boolean
        get() = prefs.getBoolean(KEY_EQ_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_EQ_ENABLED, value) }

    var equalizerPreset: Int
        get() = prefs.getInt(KEY_EQ_PRESET, PRESET_CUSTOM)
        set(value) = prefs.edit { putInt(KEY_EQ_PRESET, value) }

    var equalizerBands: List<Short>
        get() = prefs.getString(KEY_EQ_BANDS, "")!!
            .split(",")
            .mapNotNull { it.toShortOrNull() }
        set(value) = prefs.edit { putString(KEY_EQ_BANDS, value.joinToString(",")) }

    var bassBoost: Int
        get() = prefs.getInt(KEY_BASS_BOOST, 0)
        set(value) = prefs.edit { putInt(KEY_BASS_BOOST, value) }

    var virtualizer: Int
        get() = prefs.getInt(KEY_VIRTUALIZER, 0)
        set(value) = prefs.edit { putInt(KEY_VIRTUALIZER, value) }

    var loudnessGain: Int
        get() = prefs.getInt(KEY_LOUDNESS, 0)
        set(value) = prefs.edit { putInt(KEY_LOUDNESS, value) }

    companion object {
        const val PRESET_CUSTOM = -1
        private const val MAX_RECENT = 50

        private const val KEY_THEME = "theme_mode"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_FAVORITES = "favorite_track_ids"
        private const val KEY_RECENT = "recently_played"
        private const val KEY_PLAY_COUNTS = "play_counts"
        private const val KEY_SORT_PREFIX = "sort_"
        private const val KEY_SPEED = "playback_speed"
        private const val KEY_SLEEP_MINUTES = "sleep_minutes"
        private const val KEY_EQ_ENABLED = "eq_enabled"
        private const val KEY_EQ_PRESET = "eq_preset"
        private const val KEY_EQ_BANDS = "eq_bands"
        private const val KEY_BASS_BOOST = "bass_boost"
        private const val KEY_VIRTUALIZER = "virtualizer"
        private const val KEY_LOUDNESS = "loudness_gain"
    }
}
