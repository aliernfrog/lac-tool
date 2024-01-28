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
import com.aliernfrog.lactool.data.ImageFile
import com.aliernfrog.lactool.impl.Progress
import com.aliernfrog.lactool.impl.ProgressState
import com.aliernfrog.lactool.util.extension.resolvePath
import com.aliernfrog.lactool.util.manager.ContextUtils
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    private lateinit var screenshotsFile: DocumentFileCompat

    val screenshotSheetState = SheetState(skipPartiallyExpanded = false, Density(context))

    var screenshots by mutableStateOf(emptyList<ImageFile>())
    var screenshotSheetScreeenshot by mutableStateOf<ImageFile?>(null)

    fun getScreenshotsFile(context: Context): DocumentFileCompat {
        val isUpToDate = if (!::screenshotsFile.isInitialized) false
        else {
            val updatedPath = screenshotsFile.uri.resolvePath()
            val existingPath = Uri.parse(screenshotsDir).resolvePath()
            updatedPath == existingPath
        }
        if (isUpToDate) return screenshotsFile
        val treeUri = Uri.parse(screenshotsDir)
        screenshotsFile = DocumentFileCompat.fromTreeUri(context, treeUri)!!
        return screenshotsFile
    }

    suspend fun fetchScreenshots() {
        withContext(Dispatchers.IO) {
            val files = screenshotsFile.listFiles().filter { it.isFile() && it.name.lowercase().endsWith(".jpg") }.sortedBy { it.lastModified }
            screenshots = files.map {
                val nameWithoutExtension = FileUtil.removeExtension(it.name)
                ImageFile(nameWithoutExtension, it.name, screenshotsFile.findFile(it.name))
            }
        }
    }

    suspend fun deleteImportedScreenshot(screenshot: ImageFile) {
        progressState.currentProgress = Progress(
            contextUtils.getString(R.string.screenshots_deleting)
        )
        withContext(Dispatchers.IO) {
            screenshotsFile.findFile(screenshot.fileName)?.delete()
            fetchScreenshots()
            topToastState.showToast(R.string.screenshots_deleted, Icons.Rounded.Delete, TopToastColor.ERROR)
        }
        progressState.currentProgress = null
    }

    suspend fun shareImportedScreenshot(screenshot: ImageFile, context: Context) {
        withContext(Dispatchers.IO) {
            FileUtil.shareFiles(screenshot.file ?: return@withContext, context = context)
        }
    }

    suspend fun showScreenshotSheet(screenshot: ImageFile) {
        screenshotSheetScreeenshot = screenshot
        screenshotSheetState.show()
    }
}