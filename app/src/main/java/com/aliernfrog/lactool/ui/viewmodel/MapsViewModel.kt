package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.TopAppBarState
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.TAG
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.toptoast.state.TopToastState
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.aliernfrog.lactool.domain.AppState
import com.aliernfrog.lactool.domain.MapsState
import com.aliernfrog.lactool.ui.component.widget.media_overlay.maps.MapThumbnailToolbarContent
import com.aliernfrog.lactool.util.extension.showReportableErrorToast
import io.github.aliernfrog.pftool_shared.enum.MapImportedState
import io.github.aliernfrog.pftool_shared.impl.FileWrapper
import io.github.aliernfrog.pftool_shared.impl.Progress
import io.github.aliernfrog.pftool_shared.impl.ProgressState
import io.github.aliernfrog.shared.data.MediaOverlayData
import io.github.aliernfrog.shared.impl.ContextUtils
import kotlinx.coroutines.CancellationException

@Suppress("IMPLICIT_CAST_TO_ANY")
@OptIn(ExperimentalMaterial3Api::class)
class MapsViewModel(
    private val appState: AppState,
    private val mapsState: MapsState,
    private val progressState: ProgressState,
    val topToastState: TopToastState,
    private val contextUtils: ContextUtils,
    val prefs: PreferenceManager
) : ViewModel() {
    val topAppBarState = TopAppBarState(0F, 0F, 0F)

    var mapsPendingDelete
        get() = mapsState.mapsPendingDelete
        set(value) { mapsState.mapsPendingDelete = value }

    var customDialogTitleAndText
        get() = mapsState.customDialogTitleAndText
        set(value) { mapsState.customDialogTitleAndText = value }

    var activeProgress: Progress?
        get() = progressState.currentProgress
        set(value) { progressState.currentProgress = value }

    val mapsBackStack
        get() = mapsState.mapsBackStack

    fun viewMapDetails(map: Any) {
        try {
            val mapFile = when (map) {
                is MapFile -> map
                is FileWrapper -> MapFile(map)
                else -> MapFile(FileWrapper(map))
            }
            val stackupMaps = prefs.stackupMaps.value
            mapsBackStack.removeIf {
                !stackupMaps || it.path == mapFile.path
            }
            mapsBackStack.add(mapFile)
        } catch (_: CancellationException) {}
        catch (e: Exception) {
            topToastState.showReportableErrorToast(e)
            Log.e(TAG, "viewMapDetails: ", e)
        }
    }

    suspend fun deletePendingMaps(context: Context) {
        mapsPendingDelete?.let { maps ->
            if (maps.isEmpty()) return@let
            val first = maps.first()
            val total = maps.size
            val isSingle = total == 1

            var passedProgress = 0
            fun getProgress(): Progress {
                return Progress(
                    description = if (isSingle) context.getString(R.string.maps_deleting_single)
                        .replace("{NAME}", first.name)
                    else context.getString(R.string.maps_deleting_multiple)
                        .replace("{DONE}", passedProgress.toString())
                        .replace("{TOTAL}", total.toString()),
                    totalProgress = total.toLong(),
                    passedProgress = passedProgress.toLong()
                )
            }

            activeProgress = getProgress()
            first.runInIOThreadSafe {
                maps.forEach {
                    it.delete()
                    passedProgress++
                    activeProgress = getProgress()
                }
                topToastState.showToast(
                    text = if (isSingle) contextUtils.getString(R.string.maps_deleted_single).replace("{NAME}", first.name)
                    else contextUtils.getString(R.string.maps_deleted_multiple).replace("{COUNT}", maps.size.toString()),
                    icon = Icons.Rounded.Delete
                )
            }
            mapsBackStack.removeIf {
                maps.any { map ->
                    map.path == it.path
                }
            }
        }
        mapsPendingDelete = null
        loadMaps(context)
        activeProgress = null
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    fun openMapThumbnailViewer(map: MapFile) {
        val hasThumbnail = map.thumbnailModel != null
        appState.mediaOverlayData = MediaOverlayData(
            model = map.thumbnailModel,
            title = if (hasThumbnail) map.name else contextUtils.getString(R.string.maps_thumbnail_noThumbnail),
            zoomEnabled = hasThumbnail,
            toolbarContent = if (map.importedState == MapImportedState.IMPORTED) { {
                MapThumbnailToolbarContent(
                    map = map,
                    vm = this@MapsViewModel,
                    onDismissMediaOverlayRequest = {
                        appState.mediaOverlayData = null
                    }
                )
            } } else null
        )
    }

    fun loadMaps(context: Context) {
        viewModelScope.launch {
            mapsState.loadMaps(context)
        }
    }
}
