package com.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.data.prefs.ThemeMode
import com.musicplayer.data.prefs.UserPreferences
import com.musicplayer.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: UserPreferences,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = preferences.themeMode
    val dynamicColor: StateFlow<Boolean> = preferences.dynamicColor
    val playbackSpeed: StateFlow<Float> = preferences.playbackSpeed

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isRescanning = MutableStateFlow(false)
    val isRescanning: StateFlow<Boolean> = _isRescanning.asStateFlow()

    fun setThemeMode(mode: ThemeMode) = preferences.setThemeMode(mode)

    fun setDynamicColor(enabled: Boolean) = preferences.setDynamicColor(enabled)

    fun rescanLibrary() {
        if (_isRescanning.value) return
        viewModelScope.launch {
            _isRescanning.value = true
            val tracks = runCatching { mediaRepository.refresh() }.getOrNull()
            _isRescanning.value = false
            _message.value = if (tracks != null) {
                "Found ${tracks.size} songs"
            } else {
                "Could not rescan the library"
            }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
