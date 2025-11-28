package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.aliernfrog.lactool.ui.dialog.DeleteConfirmationDialog
import com.aliernfrog.lactool.util.extension.showErrorToast
import com.aliernfrog.lactool.util.manager.ContextUtils
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.lactool.util.staticutil.UriUtil
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import androidx.core.net.toUri
import com.aliernfrog.lactool.ui.dialog.CustomMessageDialog
import com.aliernfrog.lactool.util.MapsNavigationBackStack
import io.github.aliernfrog.pftool_shared.impl.Progress
import io.github.aliernfrog.pftool_shared.impl.ProgressState
import io.github.aliernfrog.pftool_shared.ui.component.ButtonIcon
import kotlinx.coroutines.CancellationException

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
    var sharedMaps by mutableStateOf(emptyList<MapFile>())
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
            mapsBackStack.add(mapFile)
            if (!prefs.stackupMaps.value) mapsBackStack.removeIf {
                it.path != mapFile.path
            }
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
        val mainViewModel = getKoinInstance<MainViewModel>()
        val hasThumbnail = map.thumbnailModel != null
        mainViewModel.showMediaView(MediaViewData(
            model = map.thumbnailModel,
            title = if (hasThumbnail) map.name else contextUtils.getString(R.string.maps_thumbnail_noThumbnail),
            zoomEnabled = hasThumbnail,
            toolbarContent = {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                var showBackupReminderDialog by remember { mutableStateOf(false) }
                var showDeleteDialog by remember { mutableStateOf(false) }

                val thumbnailPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri ->
                    if (uri != null) scope.launch {
                        activeProgress = Progress(context.getString(R.string.maps_thumbnail_setting))
                        map.runInIOThreadSafe {
                            val cachedFile = UriUtil.cacheFile(uri, "maps", context)
                            map.setThumbnailFile(context, FileWrapper(cachedFile!!))
                            viewMapDetails(map)
                            openMapThumbnailViewer(map)
                            topToastState.showToast(
                                text = R.string.maps_thumbnail_set_done,
                                icon = Icons.Default.Check
                            )
                        }
                        activeProgress = null
                    }
                }

                fun launchThumbnailPicker() {
                    thumbnailPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }

                if (hasThumbnail) IconButton(
                    onClick = { showDeleteDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shapes = IconButtonDefaults.shapes(),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.action_delete)
                    )
                }

                Button(
                    onClick = {
                        if (hasThumbnail) showBackupReminderDialog = true
                        else launchThumbnailPicker()
                    },
                    shapes = ButtonDefaults.shapes()
                ) {
                    ButtonIcon(rememberVectorPainter(Icons.Default.AddPhotoAlternate))
                    Text(stringResource(R.string.maps_thumbnail_set))
                }

                if (hasThumbnail) IconButton(
                    onClick = {
                        scope.launch {
                            activeProgress = Progress(context.getString(R.string.info_sharing))
                            map.runInIOThreadSafe {
                                FileUtil.shareFiles(map.getThumbnailFile()!!, context = context)
                            }
                            activeProgress = null
                        }
                    },
                    shapes = IconButtonDefaults.shapes(),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.action_share)
                    )
                }

                if (showBackupReminderDialog) CustomMessageDialog(
                    title = stringResource(R.string.info_reminder),
                    text = stringResource(R.string.maps_thumbnail_set_overrides),
                    dismissButtonText = stringResource(R.string.action_cancel),
                    icon = Icons.Default.Warning,
                    onDismissRequest = { showBackupReminderDialog = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                showBackupReminderDialog = false
                                launchThumbnailPicker()
                            },
                            shapes = ButtonDefaults.shapes()
                        ) {
                            Text(stringResource(R.string.action_ok))
                        }
                    }
                )

                if (showDeleteDialog) DeleteConfirmationDialog(
                    name = stringResource(R.string.maps_thumbnail_id).replace("{MAP}", map.name),
                    onDismissRequest = { showDeleteDialog = false },
                    onConfirmDelete = {
                        scope.launch {
                            activeProgress = Progress(context.getString(R.string.maps_thumbnail_deleting))
                            map.runInIOThreadSafe {
                                map.deleteThumbnailFile()
                                viewMapDetails(map)
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
        withContext(Dispatchers.Main) {
            isLoadingMaps = true
        }
        checkAndUpdateMapsFiles(context)
        val imported = fetchImportedMaps()
        val exported = fetchExportedMaps()
        withContext(Dispatchers.Main) {
            importedMaps = imported
            exportedMaps = exported
            isLoadingMaps = false
        }
    }

    /**
     * Makes sure [mapsFile] and [exportedMapsFile] are initialized and are up to date.
     */
    fun checkAndUpdateMapsFiles(context: Context) {
        getMapsFile(context)
        getExportedMapsFile(context)
    }

    /**
     * Gets [DocumentFileCompat] to imported maps folder.
     * Use this before accessing [mapsFile], otherwise the app will crash.
     */
    fun getMapsFile(context: Context): FileWrapper {
        val isUpToDate = if (!::mapsFile.isInitialized) false
        else if (lastKnownStorageAccessType != prefs.storageAccessType.value) false
        else mapsDir == mapsFile.path
        if (isUpToDate) return mapsFile
        val storageAccessType = prefs.storageAccessType.value
        lastKnownStorageAccessType = storageAccessType
        mapsFile = when (StorageAccessType.entries[storageAccessType]) {
            StorageAccessType.SAF -> {
                val treeUri = mapsDir.toUri()
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

    /**
     * Gets [DocumentFileCompat] to exported maps folder.
     * Use this before accessing [exportedMapsFile], otherwise the app will crash.
     */
    private fun getExportedMapsFile(context: Context): FileWrapper {
        val isUpToDate = if (!::exportedMapsFile.isInitialized) false
        else if (lastKnownStorageAccessType != prefs.storageAccessType.value) false
        else exportedMapsDir == exportedMapsFile.path
        if (isUpToDate) return exportedMapsFile
        val storageAccessType = prefs.storageAccessType.value
        lastKnownStorageAccessType = storageAccessType
        exportedMapsFile = when (StorageAccessType.entries[storageAccessType]) {
            StorageAccessType.SAF -> {
                val treeUri = exportedMapsDir.toUri()
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

    private fun fetchImportedMaps(): List<MapFile> {
        return mapsFile.listFiles()
            .filter { it.isFile && it.name.lowercase().endsWith(".txt") }
            .sortedBy { it.name.lowercase() }
            .map {
                MapFile(it)
            }
    }

    private fun fetchExportedMaps(): List<MapFile> {
        return exportedMapsFile.listFiles()
            .filter { it.isFile && it.name.lowercase().endsWith(".txt") }
            .sortedBy { it.name.lowercase() }
            .map {
                MapFile(it)
            }
    }
}
