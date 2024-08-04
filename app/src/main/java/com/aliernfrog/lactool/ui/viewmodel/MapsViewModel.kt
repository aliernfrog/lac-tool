package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.TAG
import com.aliernfrog.lactool.data.MapActionResult
import com.aliernfrog.lactool.di.getKoinInstance
import com.aliernfrog.lactool.enum.StorageAccessType
import com.aliernfrog.lactool.impl.FileWrapper
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.impl.Progress
import com.aliernfrog.lactool.impl.ProgressState
import com.aliernfrog.lactool.util.extension.showErrorToast
import com.aliernfrog.lactool.util.manager.ContextUtils
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Suppress("IMPLICIT_CAST_TO_ANY")
@OptIn(ExperimentalMaterial3Api::class)
class MapsViewModel(
    val topToastState: TopToastState,
    private val progressState: ProgressState,
    private val contextUtils: ContextUtils,
    val prefs: PreferenceManager
) : ViewModel() {
    val topAppBarState = TopAppBarState(0F, 0F, 0F)
    val scrollState = ScrollState(0)

    val mapsDir: String get() { return prefs.lacMapsDir }
    val exportedMapsDir: String get() { return prefs.exportedMapsDir }
    lateinit var mapsFile: FileWrapper
    lateinit var exportedMapsFile: FileWrapper

    private var lastKnownStorageAccessType = prefs.storageAccessType

    var isLoadingMaps by mutableStateOf(true)
    var importedMaps by mutableStateOf(emptyList<MapFile>())
    var exportedMaps by mutableStateOf(emptyList<MapFile>())
    var sharedMaps = mutableStateListOf<MapFile>()
    var mapsPendingDelete by mutableStateOf<List<MapFile>?>(null)
    var mapNameEdit by mutableStateOf("")
    var mapListShown by mutableStateOf(true)
    var customDialogTitleAndText: Pair<String, String>? by mutableStateOf(null)

    var activeProgress: Progress?
        get() = progressState.currentProgress
        set(value) { progressState.currentProgress = value }

    val mapListBackButtonShown
        get() = chosenMap != null

    var chosenMap by mutableStateOf<MapFile?>(null)

    fun chooseMap(map: Any?) {
        try {
            val mapToChoose = when (map) {
                is MapFile -> map
                is FileWrapper -> MapFile(map)
                else -> if (map == null) null else MapFile(FileWrapper(map))
            }
            if (mapToChoose != null) mapNameEdit = mapToChoose.name
            chosenMap = mapToChoose
        } catch (e: Exception) {
            topToastState.showErrorToast()
            Log.e(TAG, "chooseMap: ", e)
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
            chosenMap?.path?.let { path ->
                if (maps.map { it.path }.contains(path)) chooseMap(null)
            }
        }
        mapsPendingDelete = null
        loadMaps(context)
        activeProgress = null
    }

    fun resolveMapNameInput(): String {
        return mapNameEdit.ifBlank { chosenMap?.name ?: "" }
    }

    fun showActionFailedDialog(successes: List<Pair<String, MapActionResult>>, fails: List<Pair<String, MapActionResult>>) {
        customDialogTitleAndText = contextUtils.getString(R.string.maps_actionFailed)
            .replace("{SUCCESSES}", successes.size.toString())
            .replace("{FAILS}", fails.size.toString()) to fails.joinToString("\n\n") {
            "${it.first}: ${contextUtils.getString(it.second.message ?: R.string.warning_error)}"
        }
    }

    /**
     * Loads all imported and exported maps. [isLoadingMaps] will be true while this is in action.
     */
    suspend fun loadMaps(context: Context) {
        isLoadingMaps = true
        getMapsFile(context)
        getExportedMapsFile(context)
        fetchImportedMaps()
        fetchExportedMaps()
        isLoadingMaps = false
    }

    fun getMapsFile(context: Context): FileWrapper {
        val isUpToDate = if (!::mapsFile.isInitialized) false
        else if (lastKnownStorageAccessType != prefs.storageAccessType) false
        else mapsDir == mapsFile.path
        if (isUpToDate) return mapsFile
        val storageAccessType = prefs.storageAccessType
        lastKnownStorageAccessType = storageAccessType
        mapsFile = when (StorageAccessType.entries[storageAccessType]) {
            StorageAccessType.SAF -> {
                val treeUri = Uri.parse(mapsDir)
                DocumentFileCompat.fromTreeUri(context, treeUri)!!
            }
            StorageAccessType.SHIZUKU -> {
                val shizukuViewModel = getKoinInstance<ShizukuViewModel>()
                shizukuViewModel.fileService!!.getFile(mapsDir)!!
            }
            StorageAccessType.ALL_FILES -> {
                val file = File(mapsDir)
                if (!file.isDirectory) file.mkdirs()
                File(mapsDir)
            }
        }.let { FileWrapper(it) }
        return mapsFile
    }

    private fun getExportedMapsFile(context: Context): FileWrapper {
        val isUpToDate = if (!::exportedMapsFile.isInitialized) false
        else if (lastKnownStorageAccessType != prefs.storageAccessType) false
        else exportedMapsDir == exportedMapsFile.path
        if (isUpToDate) return exportedMapsFile
        val storageAccessType = prefs.storageAccessType
        lastKnownStorageAccessType = storageAccessType
        exportedMapsFile = when (StorageAccessType.entries[storageAccessType]) {
            StorageAccessType.SAF -> {
                val treeUri = Uri.parse(exportedMapsDir)
                DocumentFileCompat.fromTreeUri(context, treeUri)!!
            }
            StorageAccessType.SHIZUKU -> {
                val shizukuViewModel = getKoinInstance<ShizukuViewModel>()
                shizukuViewModel.fileService!!.getFile(exportedMapsDir)!!
            }
            StorageAccessType.ALL_FILES -> {
                val file = File(exportedMapsDir)
                if (!file.isDirectory) file.mkdirs()
                File(exportedMapsDir)
            }
        }.let { FileWrapper(it) }
        return exportedMapsFile
    }

    private suspend fun fetchImportedMaps() {
        withContext(Dispatchers.IO) {
            importedMaps = mapsFile.listFiles()
                .filter { it.isFile && it.name.lowercase().endsWith(".txt") }
                .sortedBy { it.name.lowercase() }
                .map {
                    MapFile(it)
                }
        }
    }

    private suspend fun fetchExportedMaps() {
        withContext(Dispatchers.IO) {
            exportedMaps = exportedMapsFile.listFiles()
                .filter { it.isFile && it.name.lowercase().endsWith(".txt") }
                .sortedBy { it.name.lowercase() }
                .map {
                    MapFile(it)
                }
        }
    }
}
