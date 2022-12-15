package com.aliernfrog.lactool.state

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateOf
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.data.WallpapersListItem
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WallpapersState(
    _topToastState: TopToastState,
    config: SharedPreferences
) {
    private val topToastState = _topToastState
    val lazyListState = LazyListState()
    val wallpapersDir = config.getString(ConfigKey.KEY_WALLPAPERS_DIR, ConfigKey.DEFAULT_WALLPAPERS_DIR)!!
    private lateinit var wallpapersFile: DocumentFileCompat

    val importedWallpapers = mutableStateOf(emptyList<WallpapersListItem>())
    val chosenWallpaperUri = mutableStateOf<Uri?>(null)

    fun getWallpapersFile(context: Context): DocumentFileCompat {
        if (::wallpapersFile.isInitialized) return wallpapersFile
        val treeId = wallpapersDir.replace("${Environment.getExternalStorageDirectory()}/", "primary:")
        val treeUri = DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", treeId)
        wallpapersFile = DocumentFileCompat.fromTreeUri(context, treeUri)!!
        return wallpapersFile
    }

    suspend fun getImportedWallpapers() {
        withContext(Dispatchers.IO) {
            val files = wallpapersFile.listFiles().filter { it.isFile() && it.name.lowercase().endsWith(".jpg") }.sortedBy { it.name.lowercase() }
            val wallpapers = files.map {
                val nameWithoutExtension = FileUtil.removeExtension(it.name)
                WallpapersListItem(nameWithoutExtension, it.name, wallpapersFile.findFile(it.name)?.uri.toString())
            }
            importedWallpapers.value = wallpapers
        }
    }
}