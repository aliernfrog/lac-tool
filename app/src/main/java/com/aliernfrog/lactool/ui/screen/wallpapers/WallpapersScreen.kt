package com.aliernfrog.lactool.ui.screen.wallpapers

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.HideImage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.enum.ListStyle
import com.aliernfrog.lactool.impl.FileWrapper
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.AppTopBar
import com.aliernfrog.lactool.ui.component.ErrorWithIcon
import com.aliernfrog.lactool.ui.component.FadeVisibility
import com.aliernfrog.lactool.ui.component.FloatingActionButton
import com.aliernfrog.lactool.ui.component.LazyAdaptiveVerticalGrid
import com.aliernfrog.lactool.ui.component.ImageButton
import com.aliernfrog.lactool.ui.component.ImageButtonOverlay
import com.aliernfrog.lactool.ui.component.ListViewOptionsDropdown
import com.aliernfrog.lactool.ui.component.SettingsButton
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.ui.theme.AppFABHeight
import com.aliernfrog.lactool.ui.viewmodel.WallpapersViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpapersScreen(
    wallpapersViewModel: WallpapersViewModel = koinViewModel(),
    onNavigateSettingsRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listStyle = ListStyle.entries[wallpapersViewModel.prefs.wallpapersListStyle.value]
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) scope.launch {
                wallpapersViewModel.onWallpaperPick(uri, context)
            }
        }
    )

    LaunchedEffect(Unit) {
        wallpapersViewModel.getWallpapersFile(context)
        wallpapersViewModel.fetchImportedWallpapers()
    }

    AppScaffold(
        topBar = { scrollBehavior ->
            AppTopBar(
                title = stringResource(R.string.wallpapers),
                scrollBehavior = scrollBehavior,
                actions = {
                    SettingsButton(onClick = onNavigateSettingsRequest)
                }
            )
        },
        topAppBarState = wallpapersViewModel.topAppBarState,
        floatingActionButton = {
            FloatingActionButton(
                icon = Icons.Default.Add,
                text = stringResource(R.string.wallpapers_add),
                onClick = {
                    mediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
        }
    ) {
        @Composable
        fun WallpaperButton(
            wallpaper: FileWrapper,
            modifier: Modifier = Modifier,
            contentScale: ContentScale = ContentScale.FillWidth,
            showOverlay: Boolean = true
        ) {
            ImageButton(
                model = wallpaper.painterModel,
                contentScale = contentScale,
                onClick = {
                    wallpapersViewModel.openWallpaperOptions(wallpaper)
                },
                modifier = modifier
                    .padding(8.dp)
                    .clip(AppComponentShape)
            ) {
                if (showOverlay) ImageButtonOverlay(
                    title = if (wallpaper == wallpapersViewModel.activeWallpaper) stringResource(R.string.wallpapers_list_active)
                    else wallpaper.nameWithoutExtension,
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {}
            }
        }

        @Composable
        fun ListHeader() {
            Header(
                wallpaperButton = {
                    WallpaperButton(it)
                }
            )
        }

        AnimatedContent(targetState = listStyle) { style ->
            when (style) {
                ListStyle.LIST -> LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        ListHeader()
                    }

                    items(wallpapersViewModel.wallpapersToShow) {
                        WallpaperButton(it)
                    }

                    item {
                        Footer()
                    }
                }
                ListStyle.GRID -> LazyAdaptiveVerticalGrid(
                    modifier = Modifier.fillMaxSize()
                ) { maxLineSpan: Int ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ListHeader()
                    }

                    items(wallpapersViewModel.wallpapersToShow) {
                        WallpaperButton(
                            wallpaper = it,
                            contentScale = ContentScale.Crop,
                            showOverlay = false,
                            modifier = Modifier.aspectRatio(1f)
                        )
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Footer()
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(
    wallpapersViewModel: WallpapersViewModel = koinViewModel(),
    wallpaperButton: @Composable (wallpaper: FileWrapper) -> Unit
) {
    val hasAtLeastOneWallpaper = wallpapersViewModel.wallpapersToShow.isNotEmpty()
            || wallpapersViewModel.activeWallpaper != null
    var listOptionsExpanded by remember { mutableStateOf(false) }

    Column {
        FadeVisibility(hasAtLeastOneWallpaper) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.wallpapers_clickHint),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                Box {
                    IconButton(
                        onClick = { listOptionsExpanded = true }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.list_options)
                        )
                    }
                    ListViewOptionsDropdown(
                        expanded = listOptionsExpanded,
                        onDismissRequest = { listOptionsExpanded = false },
                        sortingPref = wallpapersViewModel.prefs.wallpapersListSorting,
                        sortingReversedPref = wallpapersViewModel.prefs.wallpapersListSortingReversed,
                        stylePref = wallpapersViewModel.prefs.wallpapersListStyle
                    )
                }
            }
        }
        ErrorWithIcon(
            error = stringResource(R.string.wallpapers_noWallpapers),
            painter = rememberVectorPainter(Icons.Rounded.HideImage),
            visible = !hasAtLeastOneWallpaper,
            modifier = Modifier.fillMaxWidth()
        )
        wallpapersViewModel.activeWallpaper?.let {
            wallpaperButton(it)
        }
    }
}

@Composable
private fun Footer() {
    Spacer(Modifier.navigationBarsPadding().height(AppFABHeight))
}