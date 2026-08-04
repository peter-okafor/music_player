package com.musicplayer.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import com.musicplayer.data.prefs.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class EqualizerBand(
    val index: Int,
    val centerFrequencyHz: Int,
    val levelMillibel: Short
)

data class EqualizerState(
    val available: Boolean = false,
    val enabled: Boolean = false,
    val minLevel: Short = -1500,
    val maxLevel: Short = 1500,
    val bands: List<EqualizerBand> = emptyList(),
    val presets: List<String> = emptyList(),
    val currentPreset: Int = UserPreferences.PRESET_CUSTOM,
    val bassBoost: Int = 0,
    val virtualizer: Int = 0,
    val loudnessGain: Int = 0
)

/**
 * Wraps the platform AudioFX effects and binds them to the ExoPlayer audio
 * session. Settings are persisted so the chain is restored on the next launch.
 *
 * The player lives in the same process as the UI, so the service can hand its
 * audio session id straight to this singleton via [attachSession].
 */
@Singleton
class AudioEffectsController @Inject constructor(
    private val preferences: UserPreferences
) {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudness: LoudnessEnhancer? = null

    private var sessionId: Int = 0

    private val _state = MutableStateFlow(EqualizerState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()

    /** Called by [MusicService] once the ExoPlayer audio session exists. */
    fun attachSession(audioSessionId: Int) {
        if (audioSessionId == 0 || audioSessionId == sessionId) return
        release()
        sessionId = audioSessionId

        try {
            equalizer = Equalizer(EFFECT_PRIORITY, audioSessionId)
            bassBoost = BassBoost(EFFECT_PRIORITY, audioSessionId)
            virtualizer = Virtualizer(EFFECT_PRIORITY, audioSessionId)
            loudness = LoudnessEnhancer(audioSessionId)
            restoreSettings()
            publish()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Audio effects unavailable on this device", e)
            _state.value = EqualizerState(available = false)
        }
    }

    private fun restoreSettings() {
        val eq = equalizer ?: return
        val enabled = preferences.equalizerEnabled
        eq.enabled = enabled

        val storedPreset = preferences.equalizerPreset
        val storedBands = preferences.equalizerBands

        if (storedPreset != UserPreferences.PRESET_CUSTOM &&
            storedPreset < eq.numberOfPresets
        ) {
            runCatching { eq.usePreset(storedPreset.toShort()) }
        } else if (storedBands.size.toShort() == eq.numberOfBands) {
            storedBands.forEachIndexed { index, level ->
                runCatching { eq.setBandLevel(index.toShort(), level) }
            }
        }

        bassBoost?.let {
            it.enabled = enabled && preferences.bassBoost > 0
            runCatching { it.setStrength(preferences.bassBoost.toShort()) }
        }
        virtualizer?.let {
            it.enabled = enabled && preferences.virtualizer > 0
            runCatching { it.setStrength(preferences.virtualizer.toShort()) }
        }
        loudness?.let {
            it.enabled = enabled && preferences.loudnessGain > 0
            runCatching { it.setTargetGain(preferences.loudnessGain) }
        }
    }

    // ------------------------------------------------------------- mutations

    fun setEnabled(enabled: Boolean) {
        preferences.equalizerEnabled = enabled
        runCatching { equalizer?.enabled = enabled }
        runCatching { bassBoost?.enabled = enabled && preferences.bassBoost > 0 }
        runCatching { virtualizer?.enabled = enabled && preferences.virtualizer > 0 }
        runCatching { loudness?.enabled = enabled && preferences.loudnessGain > 0 }
        publish()
    }

    fun setBandLevel(bandIndex: Int, levelMillibel: Short) {
        val eq = equalizer ?: return
        runCatching { eq.setBandLevel(bandIndex.toShort(), levelMillibel) }
        preferences.equalizerPreset = UserPreferences.PRESET_CUSTOM
        preferences.equalizerBands = currentBandLevels()
        publish()
    }

    fun applyPreset(presetIndex: Int) {
        val eq = equalizer ?: return
        runCatching { eq.usePreset(presetIndex.toShort()) }
        preferences.equalizerPreset = presetIndex
        preferences.equalizerBands = currentBandLevels()
        publish()
    }

    fun resetBands() {
        val eq = equalizer ?: return
        for (band in 0 until eq.numberOfBands) {
            runCatching { eq.setBandLevel(band.toShort(), 0) }
        }
        preferences.equalizerPreset = UserPreferences.PRESET_CUSTOM
        preferences.equalizerBands = currentBandLevels()
        publish()
    }

    /** Strength values are 0..1000, matching the AudioFX scale. */
    fun setBassBoost(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        preferences.bassBoost = clamped
        runCatching {
            bassBoost?.enabled = preferences.equalizerEnabled && clamped > 0
            bassBoost?.setStrength(clamped.toShort())
        }
        publish()
    }

    fun setVirtualizer(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        preferences.virtualizer = clamped
        runCatching {
            virtualizer?.enabled = preferences.equalizerEnabled && clamped > 0
            virtualizer?.setStrength(clamped.toShort())
        }
        publish()
    }

    /** Gain in millibel, 0..2000. */
    fun setLoudnessGain(gainMillibel: Int) {
        val clamped = gainMillibel.coerceIn(0, 2000)
        preferences.loudnessGain = clamped
        runCatching {
            loudness?.enabled = preferences.equalizerEnabled && clamped > 0
            loudness?.setTargetGain(clamped)
        }
        publish()
    }

    // -------------------------------------------------------------- internal

    private fun currentBandLevels(): List<Short> {
        val eq = equalizer ?: return emptyList()
        return (0 until eq.numberOfBands).map { band ->
            runCatching { eq.getBandLevel(band.toShort()) }.getOrDefault(0.toShort())
        }
    }

    private fun publish() {
        val eq = equalizer
        if (eq == null) {
            _state.value = EqualizerState(available = false)
            return
        }
        try {
            val range = eq.bandLevelRange
            val bands = (0 until eq.numberOfBands).map { band ->
                EqualizerBand(
                    index = band,
                    centerFrequencyHz = eq.getCenterFreq(band.toShort()) / 1000,
                    levelMillibel = eq.getBandLevel(band.toShort())
                )
            }
            val presets = (0 until eq.numberOfPresets).map { eq.getPresetName(it.toShort()) }

            _state.value = EqualizerState(
                available = true,
                enabled = preferences.equalizerEnabled,
                minLevel = range[0],
                maxLevel = range[1],
                bands = bands,
                presets = presets,
                currentPreset = preferences.equalizerPreset,
                bassBoost = preferences.bassBoost,
                virtualizer = preferences.virtualizer,
                loudnessGain = preferences.loudnessGain
            )
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to read equalizer state", e)
            _state.value = EqualizerState(available = false)
        }
    }

    fun release() {
        runCatching { equalizer?.release() }
        runCatching { bassBoost?.release() }
        runCatching { virtualizer?.release() }
        runCatching { loudness?.release() }
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudness = null
        sessionId = 0
    }

    companion object {
        private const val TAG = "AudioEffectsController"
        private const val EFFECT_PRIORITY = 100
    }
}
