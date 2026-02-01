package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.TAG
import com.aliernfrog.lactool.domain.AppState
import com.aliernfrog.lactool.domain.MapsState
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.util.MainDestinationGroup
import com.aliernfrog.lactool.util.UpdateScreenDestination
import com.aliernfrog.lactool.util.extension.showErrorToast
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import io.github.aliernfrog.pftool_shared.impl.FileWrapper
import io.github.aliernfrog.pftool_shared.impl.Progress
import io.github.aliernfrog.pftool_shared.impl.ProgressState
import io.github.aliernfrog.pftool_shared.impl.SAFFileCreator
import io.github.aliernfrog.pftool_shared.util.extension.cacheFile
import io.github.aliernfrog.shared.impl.UpdateCheckResult
import io.github.aliernfrog.shared.impl.VersionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
class MainViewModel(
    val prefs: PreferenceManager,
    val appState: AppState,
    val mapsState: MapsState,
    val progressState: ProgressState,
    val topToastState: TopToastState,
    val versionManager: VersionManager
) : ViewModel() {
    val navigationBackStack
        get() = appState.navigationBackStack

    var currentMainDestination
        get() = appState.currentMainDestination
        set(value) { appState.currentMainDestination = value }

    val isAtMainDestination: Boolean
        get() = navigationBackStack.last() == MainDestinationGroup

    val availableUpdates = versionManager.availableUpdates
    val currentVersionInfo = versionManager.currentVersionInfo
    val isCompatibleWithLatestVersion = versionManager.isCompatibleWithLatestVersion
    val isCheckingForUpdates = versionManager.isCheckingForUpdates

    val mediaOverlayData
        get() = appState.mediaOverlayData

    init {
        prefs.lastKnownInstalledVersion.value = versionManager.currentVersionCode

        if (prefs.autoCheckUpdates.value) checkUpdates()
    }

    fun checkUpdates(
        manuallyTriggered: Boolean = false,
        skipVersionCheck: Boolean = false
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val updateCheckResult = versionManager.checkUpdates(skipVersionCheck = skipVersionCheck)
            when (updateCheckResult) {
                UpdateCheckResult.NoUpdates -> {
                    if (manuallyTriggered) withContext(Dispatchers.Main) {
                        topToastState.showToast(
                            text = R.string.updates_noUpdates,
                            icon = Icons.Rounded.Info,
                            iconTintColor = TopToastColor.ON_SURFACE
                        )
                    }
                }
                UpdateCheckResult.Error -> {
                    if (manuallyTriggered) withContext(Dispatchers.Main) {
                        topToastState.showToast(
                            text = R.string.updates_error,
                            icon = Icons.Rounded.PriorityHigh,
                            iconTintColor = TopToastColor.ERROR
                        )
                    }
                }
                is UpdateCheckResult.UpdatesAvailable -> {
                    withContext(Dispatchers.Main) {
                        appState.showUpdateNotification = true
                        if (manuallyTriggered && navigationBackStack.first() !is UpdateScreenDestination)
                            navigationBackStack.add(UpdateScreenDestination)
                        else showUpdateToast()
                    }
                }
            }
        }
    }

    fun showUpdateToast() {
        io.github.aliernfrog.shared.util.showUpdateToast {
            if (navigationBackStack.first() !is UpdateScreenDestination)
                navigationBackStack.add(UpdateScreenDestination)
        }
    }

    fun dismissMediaOverlay() {
        appState.mediaOverlayData = null
    }

    fun setSafTxtFileCreator(creator: SAFFileCreator) {
        appState.safTxtFileCreator = creator
    }

    fun handleIntent(intent: Intent, context: Context) {
        try {
            val uris: MutableList<Uri> = intent.data?.let {
                mutableListOf(it)
            } ?: mutableListOf()
            intent.clipData?.let { clipData ->
                for (i in 0..<clipData.itemCount) {
                    uris.add(clipData.getItemAt(i).uri)
                }
            }
            if (uris.isEmpty()) return

            progressState.currentProgress = Progress(context.getString(R.string.info_pleaseWait))
            viewModelScope.launch(Dispatchers.IO) {
                val cached = mutableListOf<MapFile>()
                uris.forEach { uri ->
                    val file = uri.cacheFile(context)
                    if (file != null) cached.add(MapFile(FileWrapper(file)))
                }
                if (cached.size == 1) {
                    mapsState.viewMapDetails(cached.first())
                } else if (cached.size > 1) {
                    mapsState.setSharedMaps(cached)
                }
                progressState.currentProgress = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleIntent: $e")
            topToastState.showErrorToast()
            progressState.currentProgress = null
        }
    }
}