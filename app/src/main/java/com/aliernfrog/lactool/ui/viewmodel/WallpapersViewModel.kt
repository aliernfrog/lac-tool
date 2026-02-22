package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.domain.AppState
import com.aliernfrog.lactool.ui.component.widget.media_overlay.wallpapers.ImportWallpaperSheetContent
import com.aliernfrog.lactool.ui.component.widget.media_overlay.wallpapers.WallpaperToolbarContent
import com.aliernfrog.lactool.util.extension.showErrorToast
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.aliernfrog.pftool_shared.enum.ListSorting
import io.github.aliernfrog.pftool_shared.impl.FileWrapper
import io.github.aliernfrog.pftool_shared.impl.Progress
import io.github.aliernfrog.pftool_shared.impl.ProgressState
import io.github.aliernfrog.pftool_shared.repository.FileRepository
import io.github.aliernfrog.pftool_shared.util.staticutil.PFToolSharedUtil
import io.github.aliernfrog.shared.data.MediaOverlayData
import io.github.aliernfrog.shared.impl.ContextUtils
import io.github.aliernfrog.shared.ui.component.createSheetStateWithDensity

@Suppress("IMPLICIT_CAST_TO_ANY")
@OptIn(ExperimentalMaterial3Api::class)
class WallpapersViewModel(
    val prefs: PreferenceManager,
    private val appState: AppState,
    private val progressState: ProgressState,
    val topToastState: TopToastState,
    private val contextUtils: ContextUtils,
    private val fileRepository: FileRepository,
    context: Context
) : ViewModel() {
    val topAppBarState = TopAppBarState(0F, 0F, 0F)
    val listViewOptionsSheetState = createSheetStateWithDensity(skipPartiallyExpanded = true, Density(context))

    private val activeWallpaperFileName = "mywallpaper.jpg"
    val wallpapersDir : String get() = prefs.lacWallpapersDir.value

    private var importedWallpapers by mutableStateOf(emptyList<FileWrapper>())
    var activeWallpaper by mutableStateOf<FileWrapper?>(null)

    val otherWallpapersToShow: List<FileWrapper>
        get() {
            val sorting = ListSorting.entries[prefs.wallpapersListOptions.sorting.value]
            val reversed = prefs.wallpapersListOptions.sortingReversed.value
            return importedWallpapers.sortedWith(sorting.comparator).let {
                if (reversed) it.reversed() else it
            }
        }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    fun onWallpaperPick(uri: Uri, context: Context) = viewModelScope.launch(Dispatchers.IO) {
        val file = PFToolSharedUtil.cacheFile(
            uri = uri,
            parentName = "wallpapers",
            context = context
        )?.let { FileWrapper(it) }
        if (file == null) {
            topToastState.showToast(R.string.warning_pickFile_failed, Icons.Rounded.PriorityHigh, TopToastColor.ERROR)
            return@launch
        }
        appState.mediaOverlayData = MediaOverlayData(
            model = file.painterModel,
            title = context.getString(R.string.wallpapers_chosen),
            optionsSheetContent = {
                ImportWallpaperSheetContent(
                    file = file,
                    vm = this@WallpapersViewModel,
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

    suspend fun shareImportedWallpaper(wallpaper: FileWrapper, context: Context) {
        progressState.currentProgress = Progress(
            contextUtils.getString(R.string.info_sharing)
        )
        withContext(Dispatchers.IO) {
            FileUtil.shareFiles(wallpaper, context = context)
        }
        progressState.currentProgress = null
    }

    suspend fun deleteImportedWallpaper(wallpaper: FileWrapper, context: Context) {
        progressState.currentProgress = Progress(
            contextUtils.getString(R.string.wallpapers_deleting)
        )
        withContext(Dispatchers.IO) {
            wallpaper.delete()
            fetchImportedWallpapers(context)
            topToastState.showToast(R.string.wallpapers_deleted, Icons.Rounded.Delete, TopToastColor.ERROR)
        }
        progressState.currentProgress = null
    }

    private fun getWallpapersFile(context: Context): FileWrapper? {
        return fileRepository.getFile(wallpapersDir, context)
    }

    fun fetchImportedWallpapers(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
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
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    fun openWallpaperOptions(wallpaper: FileWrapper) {
        appState.mediaOverlayData = MediaOverlayData(
            model = wallpaper.painterModel,
            title = wallpaper.name,
            toolbarContent = {
                WallpaperToolbarContent(
                    wallpaper = wallpaper,
                    vm = this@WallpapersViewModel,
                    onDismissMediaOverlayRequest = {
                        appState.mediaOverlayData = null
                    }
                )
            }
        )
    }
}