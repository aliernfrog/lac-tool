package com.aliernfrog.lactool.ui.screen.wallpapers

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.rounded.HideImage
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.ImageButton
import com.aliernfrog.lactool.ui.component.ImageButtonOverlay
import com.aliernfrog.lactool.ui.component.SettingsButton
import com.aliernfrog.lactool.ui.viewmodel.WallpapersViewModel
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.aliernfrog.pftool_shared.enum.ListStyle
import io.github.aliernfrog.pftool_shared.impl.FileWrapper
import io.github.aliernfrog.pftool_shared.ui.component.LazyAdaptiveVerticalGrid
import io.github.aliernfrog.pftool_shared.ui.sheet.ListViewOptionsSheet
import io.github.aliernfrog.shared.ui.component.AppScaffoldNoContentPadding
import io.github.aliernfrog.shared.ui.component.AppTopBar
import io.github.aliernfrog.shared.ui.component.ErrorWithIcon
import io.github.aliernfrog.shared.ui.component.FadeVisibility
import io.github.aliernfrog.shared.ui.component.IconButtonWithTooltip
import io.github.aliernfrog.shared.ui.component.SEGMENTOR_SMALL_ROUNDNESS
import io.github.aliernfrog.shared.ui.component.expressive.ExpressiveSection
import io.github.aliernfrog.shared.ui.component.util.BottomSpacer
import io.github.aliernfrog.shared.ui.component.util.LazyGridScrollAccessibilityListener
import io.github.aliernfrog.shared.ui.component.util.LazyListScrollAccessibilityListener
import io.github.aliernfrog.shared.ui.component.verticalSegmentedShape
import io.github.aliernfrog.shared.ui.theme.AppComponentShape
import io.github.aliernfrog.shared.ui.theme.AppFABPadding
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpapersScreen(
    vm: WallpapersViewModel,
    bottomPadding: Dp,
    onNavigateSettingsRequest: () -> Unit
) {
    val context = LocalContext.current
    val hazeState = rememberHazeState()
    val listStylePref = vm.prefs.wallpapersListOptions.styleGroup.getCurrent()
    val gridMaxLineSpanPref = vm.prefs.wallpapersListOptions.gridMaxLineSpanGroup.getCurrent()
    val listStyle = ListStyle.entries[listStylePref.value]
    var showFABLabel by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        vm.fetchImportedWallpapers(context)
    }

    ListViewOptionsSheet(
        sheetState = vm.listViewOptionsSheetState,
        listViewOptionsPreference = vm.prefs.wallpapersListOptions
    )

    AppScaffoldNoContentPadding(
        topBar = { scrollBehavior ->
            AppTopBar(
                title = stringResource(R.string.wallpapers),
                hazeState = hazeState,
                scrollBehavior = scrollBehavior,
                actions = {
                    SettingsButton(onClick = onNavigateSettingsRequest)
                }
            )
        },
        topAppBarState = vm.topAppBarState
    ) { paddingValues ->
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
                    vm.openWallpaperOptions(wallpaper)
                },
                modifier = modifier
            ) {
                if (showOverlay) ImageButtonOverlay(
                    title = if (wallpaper == vm.activeWallpaper) stringResource(R.string.wallpapers_list_active)
                    else wallpaper.nameWithoutExtension,
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {}
            }
        }

        @Composable
        fun ListHeader(modifier: Modifier) {
            Header(
                modifier = modifier
                    .padding(top = paddingValues.calculateTopPadding()),
                vm = vm,
                wallpaperButton = {
                    WallpaperButton(it)
                }
            )
        }

        @Composable
        fun Footer() {
            BottomSpacer(Modifier.padding(top = AppFABPadding + bottomPadding))
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
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState)
                ) {
                    item {
                        ListHeader(
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    itemsIndexed(vm.otherWallpapersToShow) { index, wallpaper ->
                        WallpaperButton(
                            wallpaper = wallpaper,
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .verticalSegmentedShape(index, totalSize = vm.otherWallpapersToShow.size)
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
                        .fillMaxSize()
                        .hazeSource(hazeState),
                    maxLineSpan = gridMaxLineSpanPref.value
                ) { maxLineSpan: Int ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ListHeader(
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }

                    items(vm.otherWallpapersToShow) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Header(
    modifier: Modifier,
    vm: WallpapersViewModel,
    wallpaperButton: @Composable (wallpaper: FileWrapper) -> Unit
) {
    val scope = rememberCoroutineScope()
    val hasAtLeastOneWallpaper = vm.otherWallpapersToShow.isNotEmpty()
            || vm.activeWallpaper != null

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
                    IconButtonWithTooltip(
                        icon = rememberVectorPainter(Icons.AutoMirrored.Filled.Sort),
                        contentDescription = stringResource(R.string.list_options),
                        onClick = { scope.launch {
                            vm.listViewOptionsSheetState.show()
                        } }
                    )
                }
            }
        }
        ErrorWithIcon(
            description = stringResource(R.string.wallpapers_noWallpapers),
            icon = rememberVectorPainter(Icons.Rounded.HideImage),
            visible = !hasAtLeastOneWallpaper,
            modifier = Modifier.fillMaxWidth()
        )
        AnimatedContent(
            targetState = vm.activeWallpaper,
            modifier = Modifier.clip(AppComponentShape)
        ) { activeWallpaper ->
            if (activeWallpaper != null) wallpaperButton(activeWallpaper)
        }
        AnimatedVisibility(vm.otherWallpapersToShow.isNotEmpty()) {
            ExpressiveSection(
                title = stringResource(R.string.wallpapers_list_other)
            ) {}
        }
    }
}