package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.di.getKoinInstance
import com.aliernfrog.lactool.enum.StorageAccessType
import com.aliernfrog.lactool.impl.FileWrapper
import com.aliernfrog.lactool.impl.Progress
import com.aliernfrog.lactool.impl.ProgressState
import com.aliernfrog.lactool.util.manager.ContextUtils
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    
    private val screenshotsDir : String get() = prefs.lacScreenshotsDir
    private lateinit var screenshotsFile: FileWrapper

    val screenshotSheetState = SheetState(skipPartiallyExpanded = false, Density(context))

    private var lastKnownStorageAccessType = prefs.storageAccessType

    var screenshots by mutableStateOf(emptyList<FileWrapper>())
    var screenshotSheetScreeenshot by mutableStateOf<FileWrapper?>(null)

    fun getScreenshotsFile(context: Context): FileWrapper {
        val isUpToDate = if (!::screenshotsFile.isInitialized) false
        else if (lastKnownStorageAccessType != prefs.storageAccessType) false
        else screenshotsDir == screenshotsFile.path
        if (isUpToDate) return screenshotsFile
        val storageAccessType = prefs.storageAccessType
        lastKnownStorageAccessType = storageAccessType
        screenshotsFile = when (StorageAccessType.entries[storageAccessType]) {
            StorageAccessType.SAF -> {
                val treeUri = Uri.parse(screenshotsDir)
                DocumentFileCompat.fromTreeUri(context, treeUri)!!
            }
            StorageAccessType.SHIZUKU -> {
                val shizukuViewModel = getKoinInstance<ShizukuViewModel>()
                shizukuViewModel.fileService!!.getFile(screenshotsDir)!!
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

    suspend fun deleteImportedScreenshot(screenshot: FileWrapper) {
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

    suspend fun shareImportedScreenshot(screenshot: FileWrapper, context: Context) {
        progressState.currentProgress = Progress(
            contextUtils.getString(R.string.info_sharing)
        )
        withContext(Dispatchers.IO) {
            FileUtil.shareFiles(screenshot, context = context)
        }
        progressState.currentProgress = null
    }

    suspend fun showScreenshotSheet(screenshot: FileWrapper) {
        screenshotSheetScreeenshot = screenshot
        screenshotSheetState.show()
    }
}