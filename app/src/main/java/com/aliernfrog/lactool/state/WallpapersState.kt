package com.aliernfrog.lactool.state

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.runtime.mutableStateOf
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.WallpapersListItem
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.lactool.util.staticutil.UriToFileUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class WallpapersState(
    _topToastState: TopToastState,
    config: SharedPreferences
) {
    private val topToastState = _topToastState
    val lazyListState = LazyListState()
    val wallpapersDir = config.getString(ConfigKey.KEY_WALLPAPERS_DIR, ConfigKey.DEFAULT_WALLPAPERS_DIR)!!
    private lateinit var wallpapersFile: DocumentFileCompat

    val importedWallpapers = mutableStateOf(emptyList<WallpapersListItem>())
    val chosenWallpaper = mutableStateOf<WallpapersListItem?>(null)

    suspend fun setChosenWallpaper(uri: Uri, context: Context) {
        withContext(Dispatchers.IO) {
            val path = UriToFileUtil.getRealFilePath(uri, context)
            if (path == null) {
                topToastState.showToast(context.getString(R.string.warning_couldntConvertToPath), iconImageVector = Icons.Rounded.PriorityHigh, iconTintColor = TopToastColor.ERROR)
                return@withContext
            }
            val file = File(path)
            chosenWallpaper.value = WallpapersListItem(
                name = file.nameWithoutExtension,
                fileName = file.name,
                painterModel = file.absolutePath
            )
        }
    }

    suspend fun importChosenWallpaper(context: Context) {
        withContext(Dispatchers.IO) {
            val outputFile = wallpapersFile.createFile("", chosenWallpaper.value!!.fileName)
            val inputStream = File(chosenWallpaper.value!!.painterModel).inputStream()
            val outputStream = context.contentResolver.openOutputStream(outputFile!!.uri)!!
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            chosenWallpaper.value = null
            getImportedWallpapers()
            topToastState.showToast(context.getString(R.string.wallpapers_chosen_imported), iconImageVector = Icons.Rounded.Download)
        }
    }

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