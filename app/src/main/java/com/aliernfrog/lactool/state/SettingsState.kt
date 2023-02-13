package com.aliernfrog.lactool.state

import android.content.SharedPreferences
import androidx.compose.foundation.ScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.experimentalSettingsRequiredClicks
import com.aliernfrog.lactool.ui.theme.Theme
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState

@OptIn(ExperimentalMaterial3Api::class)
class SettingsState(
    private val topToastState: TopToastState,
    private val config: SharedPreferences
) {
    val topAppBarState = TopAppBarState(0F, 0F, 0F)
    val scrollState = ScrollState(0)
    private var aboutClickCount = 0

    val theme = mutableStateOf(config.getInt(ConfigKey.KEY_APP_THEME, Theme.SYSTEM.int))
    val materialYou = mutableStateOf(config.getBoolean(ConfigKey.KEY_APP_MATERIAL_YOU, true))
    val showMapThumbnailsInList = mutableStateOf(config.getBoolean(ConfigKey.KEY_SHOW_MAP_THUMBNAILS_LIST, true))
    val autoCheckUpdates = mutableStateOf(config.getBoolean(ConfigKey.KEY_APP_AUTO_UPDATES, true))

    val themeOptionsExpanded = mutableStateOf(false)
    var pathOptionsDialogShown by mutableStateOf(false)
    val linksExpanded = mutableStateOf(false)
    val forceShowMaterialYouOption = mutableStateOf(false)
    var experimentalSettingsShown by mutableStateOf(false)

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

    fun setAutoCheckUpdates(newPreference: Boolean) {
        config.edit().putBoolean(ConfigKey.KEY_APP_AUTO_UPDATES, newPreference).apply()
        autoCheckUpdates.value = newPreference
    }

    fun onAboutClick() {
        if (aboutClickCount > experimentalSettingsRequiredClicks) return
        aboutClickCount++
        if (aboutClickCount == experimentalSettingsRequiredClicks) {
            experimentalSettingsShown = true
            topToastState.showToast(
                text = R.string.settings_experimental_enabled,
                icon = Icons.Rounded.Build,
                iconTintColor = TopToastColor.ON_SURFACE
            )
        }
    }
}