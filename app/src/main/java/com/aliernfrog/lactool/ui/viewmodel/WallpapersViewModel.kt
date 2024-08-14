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
import com.aliernfrog.lactool.data.exists
import com.aliernfrog.lactool.data.mkdirs
import com.aliernfrog.lactool.di.getKoinInstance
import com.aliernfrog.lactool.enum.StorageAccessType
import com.aliernfrog.lactool.impl.FileWrapper
import com.aliernfrog.lactool.impl.Progress
import com.aliernfrog.lactool.impl.ProgressState
import com.aliernfrog.lactool.util.extension.showErrorToast
import com.aliernfrog.lactool.util.manager.ContextUtils
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.lactool.util.staticutil.UriUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Suppress("IMPLICIT_CAST_TO_ANY")
@OptIn(ExperimentalMaterial3Api::class)
class WallpapersViewModel(
    val prefs: PreferenceManager,
    val topToastState: TopToastState,
    private val progressState: ProgressState,
    private val contextUtils: ContextUtils,
    context: Context
) : ViewModel() {
    val topAppBarState = TopAppBarState(0F, 0F, 0F)
    val lazyListState = LazyListState()

    val wallpapersDir : String get() = prefs.lacWallpapersDir
    private lateinit var wallpapersFile: FileWrapper

    val wallpaperSheetState = SheetState(skipPartiallyExpanded = false, Density(context))

    private var lastKnownStorageAccessType = prefs.storageAccessType

    var importedWallpapers by mutableStateOf(emptyList<FileWrapper>())
    var pickedWallpaper by mutableStateOf<FileWrapper?>(null)
    var wallpaperSheetWallpaper by mutableStateOf<FileWrapper?>(null)
    var wallpaperNameInputRaw by mutableStateOf("")
    private val wallpaperNameInput: String
        get() = wallpaperNameInputRaw.ifEmpty { pickedWallpaper?.nameWithoutExtension ?: "" }

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
            pickedWallpaper = FileWrapper(file)
            wallpaperNameInputRaw = file.nameWithoutExtension
        }
    }

    suspend fun importPickedWallpaper(context: Context) {
        val wallpaper = pickedWallpaper ?: return
        progressState.currentProgress = Progress(
            contextUtils.getString(R.string.wallpapers_chosen_importing)
        )
        withContext(Dispatchers.IO) {
            val outputName = "$wallpaperNameInput.jpg"
            var outputFile = wallpapersFile.findFile(outputName)
            if (outputFile?.exists() == true) return@withContext topToastState.showErrorToast(
                text = R.string.wallpapers_alreadyExists
            )
            outputFile = wallpapersFile.createFile(outputName) ?: return@withContext
            outputFile.copyFrom(wallpaper, context)
            pickedWallpaper = null
            wallpaperNameInputRaw = ""
            fetchImportedWallpapers()
            topToastState.showToast(R.string.wallpapers_chosen_imported, Icons.Rounded.Download)
        }
        progressState.currentProgress = null
    }

    suspend fun shareImportedWallpaper(wallpaper: FileWrapper, context: Context) {
        progressState.currentProgress = Progress(
            contextUtils.getString(R.string.info_sharing)
        )
        withContext(Dispatchers.IO) {
            FileUtil.shareFiles(wallpaper, context = context)
        }
        progressState.currentProgress = null
    }

    suspend fun deleteImportedWallpaper(wallpaper: FileWrapper) {
        progressState.currentProgress = Progress(
            contextUtils.getString(R.string.wallpapers_deleting)
        )
        withContext(Dispatchers.IO) {
            wallpaper.delete()
            fetchImportedWallpapers()
            topToastState.showToast(R.string.wallpapers_deleted, Icons.Rounded.Delete, TopToastColor.ERROR)
        }
        progressState.currentProgress = null
    }

    fun getWallpapersFile(context: Context): FileWrapper {
        val isUpToDate = if (!::wallpapersFile.isInitialized) false
        else if (lastKnownStorageAccessType != prefs.storageAccessType) false
        else wallpapersDir == wallpapersFile.path
        if (isUpToDate) return wallpapersFile
        val storageAccessType = prefs.storageAccessType
        lastKnownStorageAccessType = storageAccessType
        wallpapersFile = when (StorageAccessType.entries[storageAccessType]) {
            StorageAccessType.SAF -> {
                val treeUri = Uri.parse(wallpapersDir)
                DocumentFileCompat.fromTreeUri(context, treeUri)!!
            }
            StorageAccessType.SHIZUKU -> {
                val shizukuViewModel = getKoinInstance<ShizukuViewModel>()
                val file = shizukuViewModel.fileService!!.getFile(wallpapersDir)!!
                if (!file.exists()) file.mkdirs()
                shizukuViewModel.fileService!!.getFile(wallpapersDir)!!
            }
            StorageAccessType.ALL_FILES -> {
                val file = File(wallpapersDir)
                if (!file.isDirectory) file.mkdirs()
                File(wallpapersDir)
            }
        }.let { FileWrapper(it) }
        return wallpapersFile
    }

    suspend fun fetchImportedWallpapers() {
        withContext(Dispatchers.IO) {
            importedWallpapers = wallpapersFile.listFiles()
                .filter { it.isFile && it.name.lowercase().endsWith(".jpg") }
                .sortedBy { it.name.lowercase() }
        }
    }

    suspend fun showWallpaperSheet(wallpaper: FileWrapper) {
        wallpaperSheetWallpaper = wallpaper
        wallpaperSheetState.show()
    }
}