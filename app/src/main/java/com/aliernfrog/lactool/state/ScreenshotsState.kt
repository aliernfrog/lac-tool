package com.aliernfrog.lactool.state

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.provider.DocumentsContract
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.mutableStateOf
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.ImageFile
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScreenshotsState(
    _topToastState: TopToastState,
    config: SharedPreferences
) {
    private val topToastState = _topToastState
    val lazyListState = LazyListState()
    val screenshotsDir = config.getString(ConfigKey.KEY_SCREENSHOTS_DIR, ConfigKey.DEFAULT_SCREENSHOTS_DIR)!!
    private lateinit var screenshotsFile: DocumentFileCompat

    @OptIn(ExperimentalMaterialApi::class)
    val screenshotSheetState = ModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    
    val importedScreenshots = mutableStateOf(emptyList<ImageFile>())
    val screenshotSheetScreeenshot = mutableStateOf<ImageFile?>(null)

    suspend fun deleteImportedScreenshot(screenshot: ImageFile, context: Context) {
        withContext(Dispatchers.IO) {
            screenshotsFile.findFile(screenshot.fileName)?.delete()
            getImportedScreenshots()
            topToastState.showToast(context.getString(R.string.screenshots_deleted), iconImageVector = Icons.Rounded.Delete, iconTintColor = TopToastColor.ERROR)
        }
    }

    suspend fun shareImportedScreenshot(screenshot: ImageFile, context: Context) {
        withContext(Dispatchers.IO) {
            FileUtil.shareFile(screenshotsFile.findFile(screenshot.fileName)!!, context)
        }
    }

    fun getScreenshotsFile(context: Context): DocumentFileCompat {
        if (::screenshotsFile.isInitialized) return screenshotsFile
        val treeId = screenshotsDir.replace("${Environment.getExternalStorageDirectory()}/", "primary:")
        val treeUri = DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", treeId)
        screenshotsFile = DocumentFileCompat.fromTreeUri(context, treeUri)!!
        return screenshotsFile
    }

    suspend fun getImportedScreenshots() {
        withContext(Dispatchers.IO) {
            val files = screenshotsFile.listFiles().filter { it.isFile() && it.name.lowercase().endsWith(".jpg") }.sortedBy { it.lastModified }
            val screenshots = files.map {
                val nameWithoutExtension = FileUtil.removeExtension(it.name)
                ImageFile(nameWithoutExtension, it.name, screenshotsFile.findFile(it.name))
            }
            importedScreenshots.value = screenshots
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    suspend fun showScreenshotSheet(screenshot: ImageFile) {
        screenshotSheetScreeenshot.value = screenshot
        screenshotSheetState.show()
    }
}