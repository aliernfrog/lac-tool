package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.MediaViewData
import com.aliernfrog.lactool.data.exists
import com.aliernfrog.lactool.data.mkdirs
import com.aliernfrog.lactool.di.getKoinInstance
import com.aliernfrog.lactool.enum.StorageAccessType
import com.aliernfrog.lactool.impl.FileWrapper
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
import androidx.core.net.toUri
import com.aliernfrog.lactool.util.extension.comparator
import io.github.aliernfrog.pftool_shared.enum.ListSorting
import io.github.aliernfrog.pftool_shared.impl.Progress
import io.github.aliernfrog.pftool_shared.impl.ProgressState
import io.github.aliernfrog.pftool_shared.ui.component.ButtonIcon
import io.github.aliernfrog.pftool_shared.ui.component.createSheetStateWithDensity
import io.github.aliernfrog.pftool_shared.ui.dialog.DeleteConfirmationDialog

@Suppress("IMPLICIT_CAST_TO_ANY")
@OptIn(ExperimentalMaterial3Api::class)
class ScreenshotsViewModel(
    val prefs: PreferenceManager,
    private val topToastState: TopToastState,
    private val progressState: ProgressState,
    private val contextUtils: ContextUtils,
    context: Context
) : ViewModel() {
    val topAppBarState = TopAppBarState(0F, 0F, 0F)
    val lazyListState = LazyListState()
    val listViewOptionsSheetState = createSheetStateWithDensity(skipPartiallyExpanded = true, Density(context))
    
    private val screenshotsDir : String get() = prefs.lacScreenshotsDir.value
    private lateinit var screenshotsFile: FileWrapper

    private var lastKnownStorageAccessType = prefs.storageAccessType.value

    private var screenshots by mutableStateOf(emptyList<FileWrapper>())

    val screenshotsToShow: List<FileWrapper>
        get() {
            val sorting = ListSorting.entries[prefs.screenshotsListOptions.sorting.value]
            val reversed = prefs.screenshotsListOptions.sortingReversed.value
            return screenshots.sortedWith(sorting.comparator()).let {
                if (reversed) it.reversed() else it
            }
        }

    fun getScreenshotsFile(context: Context): FileWrapper {
        val isUpToDate = if (!::screenshotsFile.isInitialized) false
        else if (lastKnownStorageAccessType != prefs.storageAccessType.value) false
        else screenshotsDir == screenshotsFile.path
        if (isUpToDate) return screenshotsFile
        val storageAccessType = prefs.storageAccessType.value
        lastKnownStorageAccessType = storageAccessType
        screenshotsFile = when (StorageAccessType.entries[storageAccessType]) {
            StorageAccessType.SAF -> {
                val treeUri = screenshotsDir.toUri()
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
                .sortedByDescending { it.lastModified }
        }
    }

    private suspend fun deleteScreenshot(screenshot: FileWrapper) {
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

    private suspend fun shareScreenshot(screenshot: FileWrapper, context: Context) {
        progressState.currentProgress = Progress(
            contextUtils.getString(R.string.info_sharing)
        )
        withContext(Dispatchers.IO) {
            FileUtil.shareFiles(screenshot, context = context)
        }
        progressState.currentProgress = null
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    fun openScreenshotOptions(screenshot: FileWrapper) {
        val mainViewModel = getKoinInstance<MainViewModel>()
        mainViewModel.showMediaView(MediaViewData(
            model = screenshot.painterModel,
            title = screenshot.name,
            toolbarContent = {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                var showDeleteDialog by remember { mutableStateOf(false) }

                TextButton(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shapes = ButtonDefaults.shapes()
                ) {
                    ButtonIcon(rememberVectorPainter(Icons.Default.Delete))
                    Text(stringResource(R.string.action_delete))
                }

                Spacer(Modifier.width(4.dp))

                FilledTonalButton(
                    onClick = {
                        scope.launch { shareScreenshot(screenshot, context) }
                    },
                    shapes = ButtonDefaults.shapes()
                ) {
                    ButtonIcon(rememberVectorPainter(Icons.Default.Share))
                    Text(stringResource(R.string.action_share))
                }

                if (showDeleteDialog) DeleteConfirmationDialog(
                    name = screenshot.name,
                    onDismissRequest = { showDeleteDialog = false },
                    onConfirmDelete = {
                        showDeleteDialog = false
                        scope.launch { deleteScreenshot(screenshot) }
                        mainViewModel.dismissMediaView()
                    }
                )
            }
        ))
    }
}