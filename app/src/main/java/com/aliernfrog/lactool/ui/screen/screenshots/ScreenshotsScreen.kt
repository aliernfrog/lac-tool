package com.aliernfrog.lactool.ui.screen.screenshots

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.rounded.NoPhotography
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
import com.aliernfrog.lactool.ui.sheet.ListViewOptionsSheet
import com.aliernfrog.lactool.ui.viewmodel.ScreenshotsViewModel
import com.aliernfrog.lactool.util.staticutil.FileUtil
import io.github.aliernfrog.pftool_shared.enum.ListStyle
import io.github.aliernfrog.pftool_shared.ui.component.ErrorWithIcon
import io.github.aliernfrog.pftool_shared.ui.component.FadeVisibility
import io.github.aliernfrog.pftool_shared.ui.component.ImageButtonInfo
import io.github.aliernfrog.pftool_shared.ui.component.LazyAdaptiveVerticalGrid
import io.github.aliernfrog.pftool_shared.ui.component.SEGMENTOR_SMALL_ROUNDNESS
import io.github.aliernfrog.pftool_shared.ui.component.verticalSegmentedShape
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotsScreen(
    screenshotsViewModel: ScreenshotsViewModel = koinViewModel(),
    onNavigateSettingsRequest: () -> Unit
) {
    val context = LocalContext.current
    val listStylePref = screenshotsViewModel.prefs.screenshotsListOptions.styleGroup.getCurrent()
    val gridMaxLineSpanPref = screenshotsViewModel.prefs.screenshotsListOptions.gridMaxLineSpanGroup.getCurrent()
    val listStyle = ListStyle.entries[listStylePref.value]
    
    LaunchedEffect(Unit) {
        screenshotsViewModel.getScreenshotsFile(context)
        screenshotsViewModel.fetchScreenshots()
    }

    ListViewOptionsSheet(
        sheetState = screenshotsViewModel.listViewOptionsSheetState,
        listViewOptionsPreference = screenshotsViewModel.prefs.screenshotsListOptions
    )
    
    AppScaffold(
        topBar = { scrollBehavior ->
            AppTopBar(
                title = stringResource(R.string.screenshots),
                scrollBehavior = scrollBehavior,
                actions = {
                    SettingsButton(onClick = onNavigateSettingsRequest)
                }
            )
        },
        topAppBarState = screenshotsViewModel.topAppBarState
    ) {
        @Composable
        fun ScreenshotButton(
            screenshot: FileWrapper,
            modifier: Modifier = Modifier,
            contentScale: ContentScale = ContentScale.FillWidth,
            showOverlay: Boolean = true
        ) {
            var lastModified by remember { mutableStateOf("") }
            LaunchedEffect(screenshot.lastModified) {
                lastModified = FileUtil.lastModifiedFromLong(screenshot.lastModified, context)
            }

            ImageButton(
                model = screenshot.painterModel,
                contentScale = contentScale,
                onClick = {
                    screenshotsViewModel.openScreenshotOptions(screenshot)
                },
                modifier = modifier
            ) {
                if (showOverlay) ImageButtonOverlay(
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    ImageButtonInfo(
                        text = lastModified,
                        icon = rememberVectorPainter(Icons.Default.AccessTime)
                    )
                }
            }
        }
        
        AnimatedContent(targetState = listStyle) { style ->
            when (style) {
                ListStyle.LIST -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = screenshotsViewModel.lazyListState
                ) {
                    item {
                        Header(
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    itemsIndexed(screenshotsViewModel.screenshotsToShow) { index, screenshot ->
                        ScreenshotButton(
                            screenshot = screenshot,
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .verticalSegmentedShape(index, totalSize = screenshotsViewModel.screenshotsToShow.size)
                        )
                    }

                    item {
                        Footer()
                    }
                }
                ListStyle.GRID -> LazyAdaptiveVerticalGrid(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxSize(),
                    maxLineSpan = gridMaxLineSpanPref.value
                ) { maxLineSpan: Int ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Header(
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }

                    items(screenshotsViewModel.screenshotsToShow) {
                        ScreenshotButton(
                            screenshot = it,
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
    screenshotsViewModel: ScreenshotsViewModel = koinViewModel(),
    modifier: Modifier
) {
    val scope = rememberCoroutineScope()
    Column(modifier) {
        ErrorWithIcon(
            error = stringResource(R.string.screenshots_noScreenshots),
            painter = rememberVectorPainter(Icons.Rounded.NoPhotography),
            visible = screenshotsViewModel.screenshotsToShow.isEmpty(),
            modifier = Modifier.fillMaxWidth()
        )
        FadeVisibility(screenshotsViewModel.screenshotsToShow.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.screenshots_clickHint),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                Box {
                    IconButton(
                        onClick = { scope.launch {
                            screenshotsViewModel.listViewOptionsSheetState.show()
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
    }
}

@Composable
private fun Footer() {
    Spacer(Modifier.navigationBarsPadding())
}