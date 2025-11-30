package com.aliernfrog.lactool.ui.screen.wallpapers

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.HideImage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import com.aliernfrog.lactool.impl.FileWrapper
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.AppTopBar
import com.aliernfrog.lactool.ui.component.ImageButton
import com.aliernfrog.lactool.ui.component.ImageButtonOverlay
import com.aliernfrog.lactool.ui.component.SettingsButton
import com.aliernfrog.lactool.ui.component.util.LazyGridScrollAccessibilityListener
import com.aliernfrog.lactool.ui.component.util.LazyListScrollAccessibilityListener
import com.aliernfrog.lactool.ui.sheet.ListViewOptionsSheet
import com.aliernfrog.lactool.ui.viewmodel.WallpapersViewModel
import io.github.aliernfrog.pftool_shared.enum.ListStyle
import io.github.aliernfrog.pftool_shared.ui.component.ErrorWithIcon
import io.github.aliernfrog.pftool_shared.ui.component.FadeVisibility
import io.github.aliernfrog.pftool_shared.ui.component.FloatingActionButton
import io.github.aliernfrog.pftool_shared.ui.component.LazyAdaptiveVerticalGrid
import io.github.aliernfrog.pftool_shared.ui.component.SEGMENTOR_SMALL_ROUNDNESS
import io.github.aliernfrog.pftool_shared.ui.component.expressive.ExpressiveSection
import io.github.aliernfrog.pftool_shared.ui.component.verticalSegmentedShape
import io.github.aliernfrog.pftool_shared.ui.theme.AppComponentShape
import io.github.aliernfrog.pftool_shared.ui.theme.AppFABPadding
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
    val listStylePref = wallpapersViewModel.prefs.wallpapersListOptions.styleGroup.getCurrent()
    val gridMaxLineSpanPref = wallpapersViewModel.prefs.wallpapersListOptions.gridMaxLineSpanGroup.getCurrent()
    val listStyle = ListStyle.entries[listStylePref.value]
    var showFABLabel by remember { mutableStateOf(true) }

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

    ListViewOptionsSheet(
        sheetState = wallpapersViewModel.listViewOptionsSheetState,
        listViewOptionsPreference = wallpapersViewModel.prefs.wallpapersListOptions
    )

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
                showText = showFABLabel,
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
            ) {
                if (showOverlay) ImageButtonOverlay(
                    title = if (wallpaper == wallpapersViewModel.activeWallpaper) stringResource(R.string.wallpapers_list_active)
                    else wallpaper.nameWithoutExtension,
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {}
            }
        }

        @Composable
        fun ListHeader(modifier: Modifier) {
            Header(
                modifier = modifier,
                wallpaperButton = {
                    WallpaperButton(it)
                }
            )
        }

        val lazyListState = rememberLazyListState()
        val lazyGridState = rememberLazyGridState()

        LazyListScrollAccessibilityListener(
            lazyListState = lazyListState,
            onShowLabelsStateChange = { showFABLabel = it }
        )

        LazyGridScrollAccessibilityListener(
            lazyGridState = lazyGridState,
            onShowLabelsStateChange = { showFABLabel = it }
        )

        AnimatedContent(targetState = listStyle) { style ->
            when (style) {
                ListStyle.LIST -> LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        ListHeader(
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    itemsIndexed(wallpapersViewModel.otherWallpapersToShow) { index, wallpaper ->
                        WallpaperButton(
                            wallpaper = wallpaper,
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .verticalSegmentedShape(index, totalSize = wallpapersViewModel.otherWallpapersToShow.size)
                        )
                    }

                    item {
                        Footer()
                    }
                }
                ListStyle.GRID -> LazyAdaptiveVerticalGrid(
                    state = lazyGridState,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxSize(),
                    maxLineSpan = gridMaxLineSpanPref.value
                ) { maxLineSpan: Int ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ListHeader(
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }

                    items(wallpapersViewModel.otherWallpapersToShow) {
                        WallpaperButton(
                            wallpaper = it,
                            contentScale = ContentScale.Crop,
                            showOverlay = false,
                            modifier = Modifier
                                .padding(2.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(SEGMENTOR_SMALL_ROUNDNESS))
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun Header(
    modifier: Modifier,
    wallpapersViewModel: WallpapersViewModel = koinViewModel(),
    wallpaperButton: @Composable (wallpaper: FileWrapper) -> Unit
) {
    val scope = rememberCoroutineScope()
    val hasAtLeastOneWallpaper = wallpapersViewModel.otherWallpapersToShow.isNotEmpty()
            || wallpapersViewModel.activeWallpaper != null

    Column(modifier) {
        FadeVisibility(hasAtLeastOneWallpaper) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.wallpapers_clickHint),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                Box {
                    IconButton(
                        onClick = { scope.launch {
                            wallpapersViewModel.listViewOptionsSheetState.show()
                        } },
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.list_options)
                        )
                    }
                }
            }
        }
        ErrorWithIcon(
            error = stringResource(R.string.wallpapers_noWallpapers),
            painter = rememberVectorPainter(Icons.Rounded.HideImage),
            visible = !hasAtLeastOneWallpaper,
            modifier = Modifier.fillMaxWidth()
        )
        AnimatedContent(
            targetState = wallpapersViewModel.activeWallpaper,
            modifier = Modifier.clip(AppComponentShape)
        ) { activeWallpaper ->
            if (activeWallpaper != null) wallpaperButton(activeWallpaper)
        }
        AnimatedVisibility(wallpapersViewModel.otherWallpapersToShow.isNotEmpty()) {
            ExpressiveSection(
                title = stringResource(R.string.wallpapers_list_other)
            ) {}
        }
    }
}

@Composable
private fun Footer() {
    Spacer(Modifier.navigationBarsPadding().height(AppFABPadding))
}