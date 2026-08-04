package com.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.musicplayer.player.AudioEffectsController
import com.musicplayer.player.EqualizerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val audioEffects: AudioEffectsController
) : ViewModel() {

    val state: StateFlow<EqualizerState> = audioEffects.state

    fun setEnabled(enabled: Boolean) = audioEffects.setEnabled(enabled)

    fun setBandLevel(band: Int, level: Short) = audioEffects.setBandLevel(band, level)

    fun applyPreset(index: Int) = audioEffects.applyPreset(index)

    fun reset() = audioEffects.resetBands()

    fun setBassBoost(strength: Int) = audioEffects.setBassBoost(strength)

    fun setVirtualizer(strength: Int) = audioEffects.setVirtualizer(strength)

    fun setLoudness(gain: Int) = audioEffects.setLoudnessGain(gain)
}
