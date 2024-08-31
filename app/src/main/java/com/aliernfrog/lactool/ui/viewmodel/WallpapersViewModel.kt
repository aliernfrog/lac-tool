package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.MediaViewData
import com.aliernfrog.lactool.data.exists
import com.aliernfrog.lactool.data.mkdirs
import com.aliernfrog.lactool.di.getKoinInstance
import com.aliernfrog.lactool.enum.ListSorting
import com.aliernfrog.lactool.enum.StorageAccessType
import com.aliernfrog.lactool.impl.FileWrapper
import com.aliernfrog.lactool.impl.Progress
import com.aliernfrog.lactool.impl.ProgressState
import com.aliernfrog.lactool.ui.component.ButtonIcon
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.dialog.DeleteConfirmationDialog
import com.aliernfrog.lactool.util.extension.showErrorToast
import com.aliernfrog.lactool.util.manager.ContextUtils
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import com.aliernfrog.lactool.util.staticutil.UriUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Suppress("IMPLICIT_CAST_TO_ANY")
@OptIn(ExperimentalMaterial3Api::class)
class WallpapersViewModel(
    val prefs: PreferenceManager,
    val topToastState: TopToastState,
    private val progressState: ProgressState,
    private val contextUtils: ContextUtils
) : ViewModel() {
    val topAppBarState = TopAppBarState(0F, 0F, 0F)

    private val activeWallpaperFileName = "mywallpaper.jpg"
    private val wallpapersDir : String get() = prefs.lacWallpapersDir.value
    private lateinit var wallpapersFile: FileWrapper

    private var lastKnownStorageAccessType = prefs.storageAccessType.value

    private var importedWallpapers by mutableStateOf(emptyList<FileWrapper>())
    var activeWallpaper by mutableStateOf<FileWrapper?>(null)

    val wallpapersToShow: List<FileWrapper>
        get() {
            val sorting = ListSorting.entries[prefs.wallpapersListSorting.value]
            val reversed = prefs.wallpapersListSortingReversed.value
            return importedWallpapers.sortedWith(sorting.comparator).let {
                if (reversed) it.reversed() else it
            }
        }

    suspend fun onWallpaperPick(uri: Uri, context: Context) {
        val mainViewModel = getKoinInstance<MainViewModel>()
        withContext(Dispatchers.IO) {
            val file = UriUtil.cacheFile(
                uri = uri,
                parentName = "wallpapers",
                context = context
            )?.let { FileWrapper(it) }
            if (file == null) {
                topToastState.showToast(R.string.warning_pickFile_failed, Icons.Rounded.PriorityHigh, TopToastColor.ERROR)
                return@withContext
            }
            mainViewModel.showMediaView(MediaViewData(
                model = file.painterModel,
                title = context.getString(R.string.wallpapers_chosen),
                options = {
                    val scope = rememberCoroutineScope()
                    val originalName = remember { file.nameWithoutExtension }
                    var importName by remember { mutableStateOf(originalName) }

                    OutlinedTextField(
                        value = importName,
                        onValueChange = { importName = it },
                        label = {
                            Text(stringResource(R.string.wallpapers_chosen_name))
                        },
                        placeholder = {
                            Text(originalName)
                        },
                        trailingIcon = {
                            Crossfade(importName != originalName) { enabled ->
                                IconButton(
                                    onClick = { importName = originalName },
                                    enabled = enabled
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = stringResource(R.string.wallpapers_chosen_name_reset)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Button(
                            onClick = { scope.launch {
                                importWallpaper(
                                    file = file,
                                    withName = importName.ifEmpty { originalName },
                                    context = context
                                )
                                mainViewModel.dismissMediaView()
                            } }
                        ) {
                            ButtonIcon(rememberVectorPainter(Icons.Default.Download))
                            Text(stringResource(R.string.wallpapers_chosen_import))
                        }
                    }
                }
            ))
        }
    }

    private suspend fun importWallpaper(
        file: FileWrapper,
        withName: String,
        context: Context
    ) {
        val outputName = "$withName.jpg"
        progressState.currentProgress = Progress(
            contextUtils.getString(R.string.wallpapers_chosen_importing)
        )
        withContext(Dispatchers.IO) {
            var outputFile = wallpapersFile.findFile(outputName)
            if (outputFile?.exists() == true) return@withContext topToastState.showErrorToast(
                text = R.string.wallpapers_alreadyExists
            )
            outputFile = wallpapersFile.createFile(outputName) ?: return@withContext
            outputFile.copyFrom(file, context)
            fetchImportedWallpapers()
            topToastState.showToast(R.string.wallpapers_chosen_imported, Icons.Rounded.Download)
        }
        progressState.currentProgress = null
    }

    private suspend fun shareImportedWallpaper(wallpaper: FileWrapper, context: Context) {
        progressState.currentProgress = Progress(
            contextUtils.getString(R.string.info_sharing)
        )
        withContext(Dispatchers.IO) {
            FileUtil.shareFiles(wallpaper, context = context)
        }
        progressState.currentProgress = null
    }

    private suspend fun deleteImportedWallpaper(wallpaper: FileWrapper) {
        progressState.currentProgress = Progress(
            contextUtils.getString(R.string.wallpapers_deleting)
        )
        withContext(Dispatchers.IO) {
            wallpaper.delete()
            fetchImportedWallpapers()
            topToastState.showToast(R.string.wallpapers_deleted, Icons.Rounded.Delete, TopToastColor.ERROR)
        }
        progressState.currentProgress = null
    }

    fun getWallpapersFile(context: Context): FileWrapper {
        val isUpToDate = if (!::wallpapersFile.isInitialized) false
        else if (lastKnownStorageAccessType != prefs.storageAccessType.value) false
        else wallpapersDir == wallpapersFile.path
        if (isUpToDate) return wallpapersFile
        val storageAccessType = prefs.storageAccessType.value
        lastKnownStorageAccessType = storageAccessType
        wallpapersFile = when (StorageAccessType.entries[storageAccessType]) {
            StorageAccessType.SAF -> {
                val treeUri = Uri.parse(wallpapersDir)
                DocumentFileCompat.fromTreeUri(context, treeUri)!!
            }
            StorageAccessType.SHIZUKU -> {
                val shizukuViewModel = getKoinInstance<ShizukuViewModel>()
                val file = shizukuViewModel.fileService!!.getFile(wallpapersDir)!!
                if (!file.exists()) file.mkdirs()
                shizukuViewModel.fileService!!.getFile(wallpapersDir)!!
            }
            StorageAccessType.ALL_FILES -> {
                val file = File(wallpapersDir)
                if (!file.isDirectory) file.mkdirs()
                File(wallpapersDir)
            }
        }.let { FileWrapper(it) }
        return wallpapersFile
    }

    suspend fun fetchImportedWallpapers() {
        withContext(Dispatchers.IO) {
            activeWallpaper = wallpapersFile.findFile(activeWallpaperFileName)?.let {
                if (it.isFile) it else null
            }
            importedWallpapers = wallpapersFile.listFiles()
                .filter {
                    it.isFile && it.name.lowercase().endsWith(".jpg") && it.name.lowercase() != activeWallpaperFileName
                }
                .sortedBy { it.name.lowercase() }
        }
    }

    fun openWallpaperOptions(wallpaper: FileWrapper) {
        val mainViewModel = getKoinInstance<MainViewModel>()
        mainViewModel.showMediaView(MediaViewData(
            model = wallpaper.painterModel,
            title = wallpaper.name,
            options = {
                val context = LocalContext.current
                val clipboardManager = LocalClipboardManager.current
                val scope = rememberCoroutineScope()
                var showDeleteDialog by remember { mutableStateOf(false) }

                ButtonRow(
                    title = stringResource(R.string.wallpapers_copyImportUrl),
                    description = stringResource(R.string.wallpapers_copyImportUrlDescription),
                    painter = rememberVectorPainter(Icons.Rounded.ContentCopy)
                ) {
                    clipboardManager.setText(AnnotatedString(GeneralUtil.generateWallpaperImportUrl(wallpaper.name, wallpapersDir)))
                    topToastState.showToast(R.string.info_copiedToClipboard, Icons.Rounded.ContentCopy)
                }
                ButtonRow(
                    title = stringResource(R.string.wallpapers_share),
                    painter = rememberVectorPainter(Icons.Rounded.IosShare)
                ) {
                    scope.launch { shareImportedWallpaper(wallpaper, context) }
                }
                ButtonRow(
                    title = stringResource(R.string.wallpapers_delete),
                    painter = rememberVectorPainter(Icons.Rounded.Delete),
                    contentColor = MaterialTheme.colorScheme.error
                ) {
                    showDeleteDialog = true
                }

                if (showDeleteDialog) DeleteConfirmationDialog(
                    name = wallpaper.name,
                    onDismissRequest = { showDeleteDialog = false },
                    onConfirmDelete = {
                        showDeleteDialog = false
                        scope.launch { deleteImportedWallpaper(wallpaper) }
                        mainViewModel.dismissMediaView()
                    }
                )
            }
        ))
    }
}