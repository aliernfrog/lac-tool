package com.aliernfrog.lactool.ui.viewmodel

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.util.appSettingsCategories
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.toptoast.state.TopToastState
import io.github.aliernfrog.pftool_shared.impl.LocaleManager
import io.github.aliernfrog.pftool_shared.impl.ProgressState
import io.github.aliernfrog.shared.impl.VersionManager

@OptIn(ExperimentalMaterial3Api::class)
class SettingsViewModel(
    val versionManager: VersionManager,
    val localeManager: LocaleManager,
    val prefs: PreferenceManager,
    val progressState: ProgressState,
    val topToastState: TopToastState,
) : ViewModel() {
    val categories = appSettingsCategories

    val debugInfo: String
        get() = versionManager.getDebugInfo()
}