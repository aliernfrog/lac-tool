package com.aliernfrog.lactool.ui.screen.screenshots

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.rounded.NoPhotography
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.aliernfrog.lactool.ui.component.ImageButton
import com.aliernfrog.lactool.ui.component.ImageButtonInfo
import com.aliernfrog.lactool.ui.component.ImageButtonOverlay
import com.aliernfrog.lactool.ui.component.LazyAdaptiveVerticalGrid
import com.aliernfrog.lactool.ui.component.ListViewOptionsDropdown
import com.aliernfrog.lactool.ui.component.SettingsButton
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.ui.viewmodel.ScreenshotsViewModel
import com.aliernfrog.lactool.util.staticutil.FileUtil
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotsScreen(
    screenshotsViewModel: ScreenshotsViewModel = koinViewModel(),
    onNavigateSettingsRequest: () -> Unit
) {
    val context = LocalContext.current
    val listStyle = ListStyle.entries[screenshotsViewModel.prefs.screenshotsListStyle.value]
    
    LaunchedEffect(Unit) {
        screenshotsViewModel.getScreenshotsFile(context)
        screenshotsViewModel.fetchScreenshots()
    }
    
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
                    .padding(8.dp)
                    .clip(AppComponentShape)
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
                        Header()
                    }
                    items(screenshotsViewModel.screenshotsToShow) {
                        ScreenshotButton(it)
                    }
                }
                ListStyle.GRID -> LazyAdaptiveVerticalGrid(
                    modifier = Modifier.fillMaxSize()
                ) { maxLineSpan: Int ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Header()
                    }
                    items(screenshotsViewModel.screenshotsToShow) {
                        ScreenshotButton(
                            screenshot = it,
                            contentScale = ContentScale.Crop,
                            showOverlay = false,
                            modifier = Modifier.aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(
    screenshotsViewModel: ScreenshotsViewModel = koinViewModel()
) {
    var listOptionsExpanded by remember { mutableStateOf(false) }
    
    Column {
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
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.screenshots_clickHint),
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
                        sortingPref = screenshotsViewModel.prefs.screenshotsListSorting,
                        sortingReversedPref = screenshotsViewModel.prefs.screenshotsListSortingReversed,
                        stylePref = screenshotsViewModel.prefs.screenshotsListStyle
                    )
                }
            }
        }
    }
}