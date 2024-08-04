package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.experimentalSettingsRequiredClicks
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext

class SettingsViewModel(
    val prefs: PreferenceManager,
    val topToastState: TopToastState,
    context: Context
) : ViewModel() {
    private var aboutClickCount by mutableIntStateOf(0)

    val libraries = Libs.Builder().withContext(context).build().libraries

    fun onAboutClick() {
        if (prefs.experimentalOptionsEnabled) return
        aboutClickCount++
        if (aboutClickCount == experimentalSettingsRequiredClicks) {
            aboutClickCount = 0
            prefs.experimentalOptionsEnabled = true
            topToastState.showToast(
                text = R.string.settings_experimental_enabled,
                icon = Icons.Rounded.Build,
                iconTintColor = TopToastColor.ON_SURFACE
            )
        }
    }
}