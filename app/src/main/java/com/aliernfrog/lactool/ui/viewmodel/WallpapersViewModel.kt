package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PriorityHigh
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
import com.aliernfrog.lactool.util.extension.resolvePath
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.lactool.util.staticutil.UriUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
class WallpapersViewModel(
    context: Context,
    val prefs: PreferenceManager,
    val topToastState: TopToastState
) : ViewModel() {
    val topAppBarState = TopAppBarState(0F, 0F, 0F)
    val lazyListState = LazyListState()

    val wallpapersDir : String get() = prefs.lacWallpapersDir
    private lateinit var wallpapersFile: DocumentFileCompat

    val wallpaperSheetState = SheetState(skipPartiallyExpanded = false, Density(context))

    var importedWallpapers by mutableStateOf(emptyList<ImageFile>())
    var pickedWallpaper by mutableStateOf<ImageFile?>(null)
    var wallpaperSheetWallpaper by mutableStateOf<ImageFile?>(null)

    suspend fun setPickedWallpaper(uri: Uri, context: Context) {
        withContext(Dispatchers.IO) {
            val file = UriUtil.cacheFile(
                uri = uri,
                parentName = "wallpapers",
                context = context
            )
            if (file == null) {
                topToastState.showToast(R.string.warning_pickFile_failed, Icons.Rounded.PriorityHigh, TopToastColor.ERROR)
                return@withContext
            }
            pickedWallpaper = ImageFile(
                name = file.nameWithoutExtension,
                fileName = file.name,
                painterModel = file.absolutePath
            )
        }
    }

    suspend fun importPickedWallpaper(context: Context) {
        val wallpaper = pickedWallpaper ?: return
        withContext(Dispatchers.IO) {
            val outputFile = wallpapersFile.createFile("", wallpaper.name+".jpg") ?: return@withContext
            val inputStream = File(wallpaper.painterModel).inputStream()
            val outputStream = context.contentResolver.openOutputStream(outputFile.uri)!!
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            pickedWallpaper = null
            fetchImportedWallpapers()
            topToastState.showToast(R.string.wallpapers_chosen_imported, Icons.Rounded.Download)
        }
    }

    suspend fun shareImportedWallpaper(wallpaper: ImageFile, context: Context) {
        withContext(Dispatchers.IO) {
            FileUtil.shareFile(wallpaper.file ?: return@withContext, context)
        }
    }

    suspend fun deleteImportedWallpaper(wallpaper: ImageFile) {
        withContext(Dispatchers.IO) {
            wallpapersFile.findFile(wallpaper.fileName)?.delete()
            fetchImportedWallpapers()
            topToastState.showToast(R.string.wallpapers_deleted, Icons.Rounded.Delete, TopToastColor.ERROR)
        }
    }

    fun getWallpapersFile(context: Context): DocumentFileCompat {
        val isUpToDate = if (!::wallpapersFile.isInitialized) false
        else {
            val updatedPath = wallpapersFile.uri.resolvePath()
            val existingPath = Uri.parse(wallpapersDir).resolvePath()
            updatedPath == existingPath
        }
        if (isUpToDate) return wallpapersFile
        val treeUri = Uri.parse(wallpapersDir)
        wallpapersFile = DocumentFileCompat.fromTreeUri(context, treeUri)!!
        return wallpapersFile
    }

    suspend fun fetchImportedWallpapers() {
        withContext(Dispatchers.IO) {
            val files = wallpapersFile.listFiles()
                .filter { it.isFile() && it.name.lowercase().endsWith(".jpg") }
                .sortedBy { it.name.lowercase() }
            importedWallpapers = files.map {
                val nameWithoutExtension = FileUtil.removeExtension(it.name)
                ImageFile(nameWithoutExtension, it.name, wallpapersFile.findFile(it.name))
            }
        }
    }

    suspend fun showWallpaperSheet(wallpaper: ImageFile) {
        wallpaperSheetWallpaper = wallpaper
        wallpaperSheetState.show()
    }
}