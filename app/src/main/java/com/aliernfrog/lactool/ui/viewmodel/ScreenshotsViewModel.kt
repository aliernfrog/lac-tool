package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.MediaViewData
import com.aliernfrog.lactool.data.exists
import com.aliernfrog.lactool.data.mkdirs
import com.aliernfrog.lactool.di.getKoinInstance
import com.aliernfrog.lactool.enum.StorageAccessType
import com.aliernfrog.lactool.impl.FileWrapper
import com.aliernfrog.lactool.impl.Progress
import com.aliernfrog.lactool.impl.ProgressState
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.dialog.DeleteConfirmationDialog
import com.aliernfrog.lactool.util.manager.ContextUtils
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Suppress("IMPLICIT_CAST_TO_ANY")
@OptIn(ExperimentalMaterial3Api::class)
class ScreenshotsViewModel(
    val prefs: PreferenceManager,
    private val topToastState: TopToastState,
    private val progressState: ProgressState,
    private val contextUtils: ContextUtils
) : ViewModel() {
    val topAppBarState = TopAppBarState(0F, 0F, 0F)
    val lazyListState = LazyListState()
    
    private val screenshotsDir : String get() = prefs.lacScreenshotsDir.value
    private lateinit var screenshotsFile: FileWrapper

    private var lastKnownStorageAccessType = prefs.storageAccessType.value

    var screenshots by mutableStateOf(emptyList<FileWrapper>())

    fun getScreenshotsFile(context: Context): FileWrapper {
        val isUpToDate = if (!::screenshotsFile.isInitialized) false
        else if (lastKnownStorageAccessType != prefs.storageAccessType.value) false
        else screenshotsDir == screenshotsFile.path
        if (isUpToDate) return screenshotsFile
        val storageAccessType = prefs.storageAccessType.value
        lastKnownStorageAccessType = storageAccessType
        screenshotsFile = when (StorageAccessType.entries[storageAccessType]) {
            StorageAccessType.SAF -> {
                val treeUri = Uri.parse(screenshotsDir)
                DocumentFileCompat.fromTreeUri(context, treeUri)!!
            }
            StorageAccessType.SHIZUKU -> {
                val shizukuViewModel = getKoinInstance<ShizukuViewModel>()
                val file = shizukuViewModel.fileService!!.getFile(screenshotsDir)!!
                if (!file.exists()) file.mkdirs()
                shizukuViewModel.fileService!!.getFile(screenshotsDir)!!
            }
            StorageAccessType.ALL_FILES -> {
                val file = File(screenshotsDir)
                if (!file.isDirectory) file.mkdirs()
                File(screenshotsDir)
            }
        }.let { FileWrapper(it) }
        return screenshotsFile
    }

    suspend fun fetchScreenshots() {
        withContext(Dispatchers.IO) {
            screenshots = screenshotsFile.listFiles()
                .filter { it.isFile && it.name.lowercase().endsWith(".jpg") }
                .sortedBy { it.lastModified }
        }
    }

    private suspend fun deleteImportedScreenshot(screenshot: FileWrapper) {
        progressState.currentProgress = Progress(
            contextUtils.getString(R.string.screenshots_deleting)
        )
        withContext(Dispatchers.IO) {
            screenshot.delete()
            fetchScreenshots()
            topToastState.showToast(R.string.screenshots_deleted, Icons.Rounded.Delete, TopToastColor.ERROR)
        }
        progressState.currentProgress = null
    }

    private suspend fun shareImportedScreenshot(screenshot: FileWrapper, context: Context) {
        progressState.currentProgress = Progress(
            contextUtils.getString(R.string.info_sharing)
        )
        withContext(Dispatchers.IO) {
            FileUtil.shareFiles(screenshot, context = context)
        }
        progressState.currentProgress = null
    }

    fun openScreenshotOptions(screenshot: FileWrapper) {
        val mainViewModel = getKoinInstance<MainViewModel>()
        mainViewModel.showMediaView(MediaViewData(
            model = screenshot.painterModel,
            title = screenshot.name,
            options = {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                var showDeleteDialog by remember { mutableStateOf(false) }

                ButtonRow(
                    title = stringResource(R.string.screenshots_share),
                    painter = rememberVectorPainter(Icons.Rounded.IosShare)
                ) {
                    scope.launch { shareImportedScreenshot(screenshot, context) }
                }
                ButtonRow(
                    title = stringResource(R.string.screenshots_delete),
                    painter = rememberVectorPainter(Icons.Rounded.Delete),
                    contentColor = MaterialTheme.colorScheme.error
                ) {
                    showDeleteDialog = true
                }

                if (showDeleteDialog) DeleteConfirmationDialog(
                    name = screenshot.name,
                    onDismissRequest = { showDeleteDialog = false },
                    onConfirmDelete = {
                        showDeleteDialog = false
                        scope.launch { deleteImportedScreenshot(screenshot) }
                        mainViewModel.dismissMediaView()
                    }
                )
            }
        ))
    }
}