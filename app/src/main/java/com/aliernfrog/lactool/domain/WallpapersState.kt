package com.aliernfrog.lactool.domain

import android.content.Context
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.widget.media_overlay.wallpapers.ImportWallpaperSheetContent
import com.aliernfrog.lactool.util.extension.showErrorToast
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import io.github.aliernfrog.pftool_shared.impl.FileWrapper
import io.github.aliernfrog.pftool_shared.impl.Progress
import io.github.aliernfrog.pftool_shared.impl.ProgressState
import io.github.aliernfrog.pftool_shared.repository.FileRepository
import io.github.aliernfrog.pftool_shared.util.staticutil.PFToolSharedUtil
import io.github.aliernfrog.shared.data.MediaOverlayData
import io.github.aliernfrog.shared.impl.ContextUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WallpapersState(
    private val prefs: PreferenceManager,
    private val appState: AppState,
    private val progressState: ProgressState,
    private val topToastState: TopToastState,
    private val contextUtils: ContextUtils,
    private val fileRepository: FileRepository
) {
    private val activeWallpaperFileName = "mywallpaper.jpg"
    val wallpapersDir : String get() = prefs.lacWallpapersDir.value

    var activeWallpaper by mutableStateOf<FileWrapper?>(null)
    var importedWallpapers by mutableStateOf(emptyList<FileWrapper>())

    fun onWallpaperPick(uri: Uri, context: Context) {
        val file = PFToolSharedUtil.cacheFile(
            uri = uri,
            parentName = "wallpapers",
            context = context
        )?.let { FileWrapper(it) }
        if (file == null) {
            topToastState.showToast(R.string.warning_pickFile_failed, Icons.Rounded.PriorityHigh, TopToastColor.ERROR)
            return
        }
        appState.mediaOverlayData = MediaOverlayData(
            model = file.painterModel,
            title = context.getString(R.string.wallpapers_chosen),
            optionsSheetContent = {
                ImportWallpaperSheetContent(
                    file = file,
                    onDismissMediaOverlayRequest = {
                        appState.mediaOverlayData = null
                    }
                )
            }
        )
    }

    suspend fun importWallpaper(
        file: FileWrapper,
        withName: String,
        context: Context
    ) {
        val outputName = "$withName.jpg"
        progressState.currentProgress = Progress(
            contextUtils.getString(R.string.wallpapers_chosen_importing)
        )
        withContext(Dispatchers.IO) {
            val wallpapersFile = getWallpapersFile(context)
            var outputFile = wallpapersFile?.findFile(outputName)
            if (outputFile?.exists() == true) return@withContext topToastState.showErrorToast(
                text = R.string.wallpapers_alreadyExists
            )
            outputFile = wallpapersFile?.createFile(outputName) ?: return@withContext
            outputFile.copyFrom(file, context)
            fetchImportedWallpapers(context)
            topToastState.showToast(R.string.wallpapers_chosen_imported, Icons.Rounded.Download)
        }
        progressState.currentProgress = null
    }

    fun fetchImportedWallpapers(context: Context) {
        val wallpapersFile = getWallpapersFile(context)
        activeWallpaper = getWallpapersFile(context)?.findFile(activeWallpaperFileName)?.let {
            if (it.isFile) it else null
        }
        importedWallpapers = (wallpapersFile?.listFiles() ?: emptyList())
            .filter {
                it.isFile && it.name.lowercase().endsWith(".jpg") && it.name.lowercase() != activeWallpaperFileName
            }
            .sortedBy { it.name.lowercase() }
    }

    fun getWallpapersFile(context: Context): FileWrapper? {
        return fileRepository.getFile(wallpapersDir, context)
    }
}