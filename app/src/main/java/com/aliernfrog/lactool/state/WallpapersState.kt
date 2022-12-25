package com.aliernfrog.lactool.state

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.runtime.mutableStateOf
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.ImageFile
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

    @OptIn(ExperimentalMaterialApi::class)
    val wallpaperSheetState = ModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val importedWallpapers = mutableStateOf(emptyList<ImageFile>())
    val pickedWallpaper = mutableStateOf<ImageFile?>(null)
    val wallpaperSheetWallpaper = mutableStateOf<ImageFile?>(null)

    suspend fun setPickedWallpaper(uri: Uri, context: Context) {
        withContext(Dispatchers.IO) {
            val path = UriToFileUtil.getRealFilePath(uri, context)
            if (path == null) {
                topToastState.showToast(R.string.warning_couldntConvertToPath, Icons.Rounded.PriorityHigh, TopToastColor.ERROR)
                return@withContext
            }
            val file = File(path)
            pickedWallpaper.value = ImageFile(
                name = file.nameWithoutExtension,
                fileName = file.name,
                painterModel = file.absolutePath
            )
        }
    }

    suspend fun importPickedWallpaper(context: Context) {
        withContext(Dispatchers.IO) {
            val outputFile = wallpapersFile.createFile("", pickedWallpaper.value!!.name+".jpg")
            val inputStream = File(pickedWallpaper.value!!.painterModel).inputStream()
            val outputStream = context.contentResolver.openOutputStream(outputFile!!.uri)!!
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            pickedWallpaper.value = null
            getImportedWallpapers()
            topToastState.showToast(R.string.wallpapers_chosen_imported, Icons.Rounded.Download)
        }
    }

    suspend fun shareImportedWallpaper(wallpaper: ImageFile, context: Context) {
        withContext(Dispatchers.IO) {
            FileUtil.shareFile(wallpapersFile.findFile(wallpaper.fileName)!!, context)
        }
    }

    suspend fun deleteImportedWallpaper(wallpaper: ImageFile) {
        withContext(Dispatchers.IO) {
            wallpapersFile.findFile(wallpaper.fileName)?.delete()
            getImportedWallpapers()
            topToastState.showToast(R.string.wallpapers_deleted, Icons.Rounded.Delete, TopToastColor.ERROR)
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
                ImageFile(nameWithoutExtension, it.name, wallpapersFile.findFile(it.name))
            }
            importedWallpapers.value = wallpapers
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    suspend fun showWallpaperSheet(wallpaper: ImageFile) {
        wallpaperSheetWallpaper.value = wallpaper
        wallpaperSheetState.show()
    }
}