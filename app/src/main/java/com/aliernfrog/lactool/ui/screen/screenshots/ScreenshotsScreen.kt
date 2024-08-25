package com.aliernfrog.lactool.ui.screen.screenshots

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.rounded.NoPhotography
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.AppTopBar
import com.aliernfrog.lactool.ui.component.ErrorWithIcon
import com.aliernfrog.lactool.ui.component.FadeVisibility
import com.aliernfrog.lactool.ui.component.ImageButton
import com.aliernfrog.lactool.ui.component.ImageButtonInfo
import com.aliernfrog.lactool.ui.component.ImageButtonOverlay
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = screenshotsViewModel.lazyListState
        ) {
            item {
                ErrorWithIcon(
                    error = stringResource(R.string.screenshots_noScreenshots),
                    painter = rememberVectorPainter(Icons.Rounded.NoPhotography),
                    visible = screenshotsViewModel.screenshots.isEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                FadeVisibility(screenshotsViewModel.screenshots.isNotEmpty()) {
                    Text(stringResource(R.string.screenshots_clickHint), Modifier.padding(8.dp))
                }
            }
            items(screenshotsViewModel.screenshots) {
                var lastModified by remember { mutableStateOf("") }
                LaunchedEffect(it.lastModified) {
                    lastModified = FileUtil.lastModifiedFromLong(it.lastModified, context)
                }

                ImageButton(
                    model = it.painterModel,
                    contentScale = ContentScale.FillWidth,
                    onClick = {
                        screenshotsViewModel.openScreenshotOptions(it)
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(AppComponentShape)
                ) {
                    ImageButtonOverlay(
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        ImageButtonInfo(
                            text = lastModified,
                            icon = rememberVectorPainter(Icons.Default.AccessTime)
                        )
                    }
                }
            }
        }
    }
}