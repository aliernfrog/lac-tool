package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.domain.AppState
import com.aliernfrog.lactool.domain.WallpapersState
import com.aliernfrog.lactool.ui.component.widget.media_overlay.wallpapers.WallpaperToolbarContent
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.github.aliernfrog.pftool_shared.enum.ListSorting
import io.github.aliernfrog.pftool_shared.impl.FileWrapper
import io.github.aliernfrog.pftool_shared.impl.Progress
import io.github.aliernfrog.pftool_shared.impl.ProgressState
import io.github.aliernfrog.shared.data.MediaOverlayData
import io.github.aliernfrog.shared.impl.ContextUtils
import io.github.aliernfrog.shared.ui.component.createSheetStateWithDensity

@Suppress("IMPLICIT_CAST_TO_ANY")
@OptIn(ExperimentalMaterial3Api::class)
class WallpapersViewModel(
    val prefs: PreferenceManager,
    private val appState: AppState,
    private val wallpapersState: WallpapersState,
    private val progressState: ProgressState,
    val topToastState: TopToastState,
    private val contextUtils: ContextUtils,
    context: Context
) : ViewModel() {
    val topAppBarState = TopAppBarState(0F, 0F, 0F)
    val listViewOptionsSheetState = createSheetStateWithDensity(skipPartiallyExpanded = true, Density(context))

    val wallpapersDir : String get() = prefs.lacWallpapersDir.value

    private var importedWallpapers
        get() = wallpapersState.importedWallpapers
        set(value) { wallpapersState.importedWallpapers = value }

    var activeWallpaper: FileWrapper?
        get() = wallpapersState.activeWallpaper
        set(value) { wallpapersState.activeWallpaper = value }

    val otherWallpapersToShow: List<FileWrapper>
        get() {
            val sorting = ListSorting.entries[prefs.wallpapersListOptions.sorting.value]
            val reversed = prefs.wallpapersListOptions.sortingReversed.value
            return importedWallpapers.sortedWith(sorting.comparator).let {
                if (reversed) it.reversed() else it
            }
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

    fun fetchImportedWallpapers(context: Context) = wallpapersState.fetchImportedWallpapers(context)

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