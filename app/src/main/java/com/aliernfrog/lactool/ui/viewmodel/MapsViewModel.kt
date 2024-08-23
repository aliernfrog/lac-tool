package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.TAG
import com.aliernfrog.lactool.data.MapActionResult
import com.aliernfrog.lactool.data.MediaViewData
import com.aliernfrog.lactool.data.exists
import com.aliernfrog.lactool.data.mkdirs
import com.aliernfrog.lactool.di.getKoinInstance
import com.aliernfrog.lactool.enum.StorageAccessType
import com.aliernfrog.lactool.impl.FileWrapper
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.impl.Progress
import com.aliernfrog.lactool.impl.ProgressState
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.dialog.DeleteConfirmationDialog
import com.aliernfrog.lactool.util.extension.showErrorToast
import com.aliernfrog.lactool.util.manager.ContextUtils
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.UriUtil
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    val mapsDir: String get() { return prefs.lacMapsDir.value }
    val exportedMapsDir: String get() { return prefs.exportedMapsDir.value }
    lateinit var mapsFile: FileWrapper
    lateinit var exportedMapsFile: FileWrapper

    private var lastKnownStorageAccessType = prefs.storageAccessType.value

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

    fun openMapThumbnailViewer(map: MapFile) {
        val mainViewModel = getKoinInstance<MainViewModel>()
        val hasThumbnail = map.thumbnailModel != null
        mainViewModel.showMediaView(MediaViewData(
            model = map.thumbnailModel,
            title = if (hasThumbnail) map.name else contextUtils.getString(R.string.maps_thumbnail_noThumbnail),
            zoomEnabled = hasThumbnail,
            options = {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                var showDeleteDialog by remember { mutableStateOf(false) }

                val thumbnailPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri ->
                    if (uri != null) scope.launch {
                        activeProgress = Progress(context.getString(R.string.maps_thumbnail_setting))
                        map.runInIOThreadSafe {
                            val cachedFile = UriUtil.cacheFile(uri, "maps", context)
                            map.setThumbnailFile(context, FileWrapper(cachedFile!!))
                            chooseMap(map)
                            openMapThumbnailViewer(map)
                            topToastState.showToast(
                                text = R.string.maps_thumbnail_set_done,
                                icon = Icons.Default.Check
                            )
                        }
                        activeProgress = null
                    }
                }

                ButtonRow(
                    title = stringResource(R.string.maps_thumbnail_set),
                    painter = rememberVectorPainter(Icons.Default.AddPhotoAlternate)
                ) {
                    thumbnailPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }

                if (hasThumbnail) ButtonRow(
                    title = stringResource(R.string.maps_thumbnail_delete),
                    painter = rememberVectorPainter(Icons.Default.Delete),
                    contentColor = MaterialTheme.colorScheme.error
                ) {
                    showDeleteDialog = true
                }

                if (showDeleteDialog) DeleteConfirmationDialog(
                    name = stringResource(R.string.maps_thumbnail_id)
                        .replace("{MAP}", map.name),
                    onDismissRequest = { showDeleteDialog = false },
                    onConfirmDelete = {
                        scope.launch {
                            activeProgress = Progress(context.getString(R.string.maps_thumbnail_deleting))
                            map.runInIOThreadSafe {
                                map.deleteThumbnailFile()
                                chooseMap(map)
                                mainViewModel.dismissMediaView()
                                showDeleteDialog = false
                                topToastState.showToast(
                                    text = R.string.maps_thumbnail_deleted,
                                    icon = Icons.Default.Delete
                                )
                            }
                            activeProgress = null
                        }
                    }
                )
            }
        ))
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
        else if (lastKnownStorageAccessType != prefs.storageAccessType.value) false
        else mapsDir == mapsFile.path
        if (isUpToDate) return mapsFile
        val storageAccessType = prefs.storageAccessType.value
        lastKnownStorageAccessType = storageAccessType
        mapsFile = when (StorageAccessType.entries[storageAccessType]) {
            StorageAccessType.SAF -> {
                val treeUri = Uri.parse(mapsDir)
                DocumentFileCompat.fromTreeUri(context, treeUri)!!
            }
            StorageAccessType.SHIZUKU -> {
                val shizukuViewModel = getKoinInstance<ShizukuViewModel>()
                val file = shizukuViewModel.fileService!!.getFile(mapsDir)!!
                if (!file.exists()) file.mkdirs()
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
        else if (lastKnownStorageAccessType != prefs.storageAccessType.value) false
        else exportedMapsDir == exportedMapsFile.path
        if (isUpToDate) return exportedMapsFile
        val storageAccessType = prefs.storageAccessType.value
        lastKnownStorageAccessType = storageAccessType
        exportedMapsFile = when (StorageAccessType.entries[storageAccessType]) {
            StorageAccessType.SAF -> {
                val treeUri = Uri.parse(exportedMapsDir)
                DocumentFileCompat.fromTreeUri(context, treeUri)!!
            }
            StorageAccessType.SHIZUKU -> {
                val shizukuViewModel = getKoinInstance<ShizukuViewModel>()
                val file = shizukuViewModel.fileService!!.getFile(exportedMapsDir)!!
                if (!file.exists()) file.mkdirs()
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
