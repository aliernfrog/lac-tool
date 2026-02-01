package com.aliernfrog.lactool.domain

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.TAG
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.util.MapsNavigationBackStack
import com.aliernfrog.lactool.util.extension.showErrorToast
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.toptoast.state.TopToastState
import io.github.aliernfrog.pftool_shared.data.MapActionResult
import io.github.aliernfrog.pftool_shared.data.getDefaultMapsListSegments
import io.github.aliernfrog.pftool_shared.impl.FileWrapper
import io.github.aliernfrog.pftool_shared.repository.MapRepository
import io.github.aliernfrog.shared.impl.ContextUtils
import kotlinx.coroutines.CancellationException

class MapsState(
    private val prefs: PreferenceManager,
    private val topToastState: TopToastState,
    private val contextUtils: ContextUtils,
    private val mapRepository: MapRepository
) {
    val mapsDir: String
        get() = prefs.lacMapsDir.value

    val exportedMapsDir: String
        get() = prefs.exportedMapsDir.value

    val mapsBackStack = MapsNavigationBackStack()
    var mapsPendingDelete by mutableStateOf<List<MapFile>?>(null)
    var customDialogTitleAndText: Pair<String, String>? by mutableStateOf(null)
    var availableSegments by mutableStateOf(
        getDefaultMapsListSegments(
            includeSharedMapsSegment = mapRepository.sharedMaps.value.isNotEmpty()
        )
    )

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
            Log.e(TAG, "MapsState/viewMapDetails: ", e)
        }
    }

    fun showActionFailedDialog(successes: List<Pair<String, MapActionResult>>, fails: List<Pair<String, MapActionResult>>) {
        customDialogTitleAndText = contextUtils.getString(R.string.maps_actionFailed)
            .replace("{SUCCESSES}", successes.size.toString())
            .replace("{FAILS}", fails.size.toString()) to fails.joinToString("\n\n") {
            "${it.first}: ${contextUtils.getString(it.second.message ?: R.string.warning_error)}"
        }
    }

    suspend fun loadMaps(context: Context) {
        mapRepository.reloadMaps(context)
    }

    fun getMapsFile(context: Context): FileWrapper? {
        return mapRepository.getImportedMapsFile(context)
    }

    fun getExportedMapsFile(context: Context): FileWrapper? {
        return mapRepository.getExportedMapsFile(context)
    }

    fun setSharedMaps(maps: List<MapFile>) {
        mapRepository.setSharedMaps(maps)
        availableSegments = getDefaultMapsListSegments(
            includeSharedMapsSegment = maps.isNotEmpty()
        )
    }
}