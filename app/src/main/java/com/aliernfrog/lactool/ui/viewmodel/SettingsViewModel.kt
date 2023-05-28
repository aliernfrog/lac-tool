package com.aliernfrog.lactool.ui.viewmodel

import androidx.compose.foundation.ScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.experimentalSettingsRequiredClicks
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState

@OptIn(ExperimentalMaterial3Api::class)
class SettingsViewModel(
    val prefs: PreferenceManager,
    val topToastState: TopToastState
) : ViewModel() {
    val topAppBarState = TopAppBarState(0F, 0F, 0F)
    val scrollState = ScrollState(0)

    var themeOptionsExpanded by mutableStateOf(false)
    var linksExpanded by mutableStateOf(false)
    var pathOptionsDialogShown by mutableStateOf(false)
    var experimentalSettingsShown by mutableStateOf(false)
    var forceShowMaterialYouOption by mutableStateOf(false)
    var aboutClickCount by mutableIntStateOf(0)

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