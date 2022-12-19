package com.aliernfrog.lactool.state

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.provider.DocumentsContract
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateOf
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.data.ImageFile
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScreenshotsState(config: SharedPreferences) {
    val lazyListState = LazyListState()
    val screenshotsDir = config.getString(ConfigKey.KEY_SCREENSHOTS_DIR, ConfigKey.DEFAULT_SCREENSHOTS_DIR)!!
    private lateinit var screenshotsFile: DocumentFileCompat
    
    val importedScreenshots = mutableStateOf(emptyList<ImageFile>())

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
                ImageFile(nameWithoutExtension, it.name, screenshotsFile.findFile(it.name)?.uri.toString())
            }
            importedScreenshots.value = screenshots
        }
    }
}