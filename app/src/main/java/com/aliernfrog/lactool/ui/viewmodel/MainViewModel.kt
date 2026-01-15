package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.TAG
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.util.MainDestinationGroup
import com.aliernfrog.lactool.util.NavigationConstant
import com.aliernfrog.lactool.util.extension.showErrorToast
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import io.github.aliernfrog.pftool_shared.impl.FileWrapper
import io.github.aliernfrog.pftool_shared.impl.Progress
import io.github.aliernfrog.pftool_shared.impl.ProgressState
import io.github.aliernfrog.pftool_shared.impl.SAFFileCreator
import io.github.aliernfrog.pftool_shared.util.extension.cacheFile
import io.github.aliernfrog.shared.data.MediaOverlayData
import io.github.aliernfrog.shared.di.getKoinInstance
import io.github.aliernfrog.shared.impl.UpdateCheckResult
import io.github.aliernfrog.shared.impl.VersionManager
import io.github.aliernfrog.shared.ui.component.createSheetStateWithDensity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
class MainViewModel(
    val prefs: PreferenceManager,
    val topToastState: TopToastState,
    val progressState: ProgressState,
    val versionManager: VersionManager,
    context: Context
) : ViewModel() {
    lateinit var scope: CoroutineScope
    lateinit var safTxtFileCreator: SAFFileCreator
    val updateSheetState = createSheetStateWithDensity(skipPartiallyExpanded = false, Density(context))

    val navigationBackStack = mutableStateListOf<Any>(
        NavigationConstant.INITIAL_DESTINATION
    )
    var currentMainDestination by mutableStateOf(NavigationConstant.INITIAL_MAIN_DESTINATION)
    val isAtMainDestination: Boolean
        get() = navigationBackStack.last() == MainDestinationGroup

    val latestVersionInfo = versionManager.latestVersionInfo
    val updateAvailable = versionManager.updateAvailable
    var showUpdateNotification by mutableStateOf(false)

    var mediaOverlayData by mutableStateOf<MediaOverlayData?>(null)
        private set

    init {
        prefs.lastKnownInstalledVersion.value = versionManager.currentVersionCode
    }

    suspend fun checkUpdates(
        manuallyTriggered: Boolean = false,
        skipVersionCheck: Boolean = false
    ) {
        withContext(Dispatchers.IO) {
            val updateCheckResult = versionManager.checkUpdates(skipVersionCheck = skipVersionCheck)
            when (updateCheckResult) {
                UpdateCheckResult.NoUpdates -> {
                    if (manuallyTriggered) withContext(Dispatchers.Main) {
                        topToastState.showAndroidToast(
                            text = R.string.updates_noUpdates,
                            icon = Icons.Rounded.Info,
                            iconTintColor = TopToastColor.ON_SURFACE
                        )
                    }
                }
                UpdateCheckResult.Error -> {
                    if (manuallyTriggered) withContext(Dispatchers.Main) {
                        topToastState.showAndroidToast(
                            text = R.string.updates_error,
                            icon = Icons.Rounded.PriorityHigh,
                            iconTintColor = TopToastColor.ERROR
                        )
                    }
                }
                is UpdateCheckResult.UpdateAvailable -> {
                    withContext(Dispatchers.Main) {
                        if (manuallyTriggered) updateSheetState.show()
                        else showUpdateToast()
                    }
                }
            }
        }
    }

    fun showUpdateToast() {
        io.github.aliernfrog.shared.util.showUpdateToast {
            scope.launch { updateSheetState.show() }
        }
    }

    fun showMediaOverlay(data: MediaOverlayData) {
        mediaOverlayData = data
    }

    fun dismissMediaOverlay() {
        mediaOverlayData = null
    }

    fun handleIntent(intent: Intent, context: Context) {
        val mapsViewModel = getKoinInstance<MapsViewModel>()

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
                    mapsViewModel.viewMapDetails(cached.first())
                } else if (cached.size > 1) {
                    mapsViewModel.setSharedMaps(cached)
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