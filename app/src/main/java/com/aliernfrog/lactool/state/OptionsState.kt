package com.aliernfrog.lactool.state

import android.content.SharedPreferences
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.mutableStateOf
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.Theme

class OptionsState(_config: SharedPreferences) {
    private val config = _config
    val scrollState = ScrollState(0)

    val theme = mutableStateOf(config.getInt(ConfigKey.KEY_APP_THEME, Theme.SYSTEM))
    val materialYou = mutableStateOf(config.getBoolean(ConfigKey.KEY_APP_MATERIAL_YOU, true))
    val showMapThumbnailsInList = mutableStateOf(config.getBoolean(ConfigKey.KEY_SHOW_MAP_THUMBNAILS_LIST, true))

    val linksExpanded = mutableStateOf(false)
    val aboutClickCount = mutableStateOf(0)
    val forceShowMaterialYouOption = mutableStateOf(false)

    fun setTheme(newTheme: Int) {
        config.edit().putInt(ConfigKey.KEY_APP_THEME, newTheme).apply()
        theme.value = newTheme
    }

    fun setMaterialYou(newPreference: Boolean) {
        config.edit().putBoolean(ConfigKey.KEY_APP_MATERIAL_YOU, newPreference).apply()
        materialYou.value = newPreference
    }

    fun setShowMapThumbnailsInList(newPreference: Boolean) {
        config.edit().putBoolean(ConfigKey.KEY_SHOW_MAP_THUMBNAILS_LIST, newPreference).apply()
        showMapThumbnailsInList.value = newPreference
    }
}