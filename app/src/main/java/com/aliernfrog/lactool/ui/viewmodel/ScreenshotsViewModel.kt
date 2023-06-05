package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.ImageFile
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
class ScreenshotsViewModel(
    context: Context,
    prefs: PreferenceManager,
    private val topToastState: TopToastState,
) : ViewModel() {
    val topAppBarState = TopAppBarState(0F, 0F, 0F)
    val lazyListState = LazyListState()
    val screenshotsDir = prefs.lacScreenshotsDir
    private val screenshotsFile = GeneralUtil.getDocumentFileFromPath(screenshotsDir, context)

    val screenshotSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden, Density(context))

    var screenshots by mutableStateOf(emptyList<ImageFile>())
    var screenshotSheetScreeenshot by mutableStateOf<ImageFile?>(null)

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
        withContext(Dispatchers.IO) {
            screenshotsFile.findFile(screenshot.fileName)?.delete()
            fetchScreenshots()
            topToastState.showToast(R.string.screenshots_deleted, Icons.Rounded.Delete, TopToastColor.ERROR)
        }
    }

    suspend fun shareImportedScreenshot(screenshot: ImageFile, context: Context) {
        withContext(Dispatchers.IO) {
            FileUtil.shareFile(screenshot.file ?: return@withContext, context)
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    suspend fun showScreenshotSheet(screenshot: ImageFile) {
        screenshotSheetScreeenshot = screenshot
        screenshotSheetState.show()
    }
}