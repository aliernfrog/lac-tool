package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.TAG
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.util.extension.showErrorToast
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.toptoast.state.TopToastState
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.aliernfrog.lactool.ui.component.widget.media_overlay.maps.MapThumbnailToolbarContent
import com.aliernfrog.lactool.util.MapsNavigationBackStack
import io.github.aliernfrog.pftool_shared.data.MapActionResult
import io.github.aliernfrog.pftool_shared.enum.MapImportedState
import io.github.aliernfrog.pftool_shared.impl.FileWrapper
import io.github.aliernfrog.pftool_shared.impl.Progress
import io.github.aliernfrog.pftool_shared.impl.ProgressState
import io.github.aliernfrog.pftool_shared.repository.MapRepository
import io.github.aliernfrog.shared.data.MediaOverlayData
import io.github.aliernfrog.shared.di.getKoinInstance
import io.github.aliernfrog.shared.impl.ContextUtils
import kotlinx.coroutines.CancellationException

@Suppress("IMPLICIT_CAST_TO_ANY")
@OptIn(ExperimentalMaterial3Api::class)
class MapsViewModel(
    val topToastState: TopToastState,
    private val progressState: ProgressState,
    private val contextUtils: ContextUtils,
    private val mapRepository: MapRepository,
    val prefs: PreferenceManager
) : ViewModel() {
    val topAppBarState = TopAppBarState(0F, 0F, 0F)

    val mapsDir: String get() { return prefs.lacMapsDir.value }
    val exportedMapsDir: String get() { return prefs.exportedMapsDir.value }

    var mapsPendingDelete by mutableStateOf<List<MapFile>?>(null)
    var customDialogTitleAndText: Pair<String, String>? by mutableStateOf(null)

    var activeProgress: Progress?
        get() = progressState.currentProgress
        set(value) { progressState.currentProgress = value }

    val mapsBackStack = MapsNavigationBackStack()

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
            topToastState.showErrorToast()
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
        // TODO remove MainViewModel dependency
        val mainViewModel = getKoinInstance<MainViewModel>()
        val hasThumbnail = map.thumbnailModel != null
        mainViewModel.showMediaOverlay(MediaOverlayData(
            model = map.thumbnailModel,
            title = if (hasThumbnail) map.name else contextUtils.getString(R.string.maps_thumbnail_noThumbnail),
            zoomEnabled = hasThumbnail,
            toolbarContent = if (map.importedState == MapImportedState.IMPORTED) { {
                MapThumbnailToolbarContent(
                    map = map,
                    onDismissMediaOverlayRequest = {
                        mainViewModel.dismissMediaOverlay()
                    }
                )
            } } else null
        ))
    }

    fun showActionFailedDialog(successes: List<Pair<String, MapActionResult>>, fails: List<Pair<String, MapActionResult>>) {
        customDialogTitleAndText = contextUtils.getString(R.string.maps_actionFailed)
            .replace("{SUCCESSES}", successes.size.toString())
            .replace("{FAILS}", fails.size.toString()) to fails.joinToString("\n\n") {
            "${it.first}: ${contextUtils.getString(it.second.message ?: R.string.warning_error)}"
        }
    }

    fun loadMaps(context: Context) {
        viewModelScope.launch {
            mapRepository.reloadMaps(context)
        }
    }

    fun getMapsFile(context: Context): FileWrapper? {
        return mapRepository.getImportedMapsFile(context)
    }

    fun getExportedMapsFile(context: Context): FileWrapper? {
        return mapRepository.getExportedMapsFile(context)
    }

    fun setSharedMaps(maps: List<MapFile>) {
        mapRepository.setSharedMaps(maps)
    }
}
