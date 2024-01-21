package com.aliernfrog.lactool.ui.screen.screenshots

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NoPhotography
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.ErrorWithIcon
import com.aliernfrog.lactool.ui.component.FadeVisibility
import com.aliernfrog.lactool.ui.component.ImageButton
import com.aliernfrog.lactool.ui.sheet.ScreenshotsSheet
import com.aliernfrog.lactool.ui.viewmodel.ScreenshotsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotsScreen(
    screenshotsViewModel: ScreenshotsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        screenshotsViewModel.getScreenshotsFile(context)
        screenshotsViewModel.fetchScreenshots()
    }
    AppScaffold(
        title = stringResource(R.string.screenshots),
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
                    visible = screenshotsViewModel.screenshots.isEmpty()
                )
                FadeVisibility(screenshotsViewModel.screenshots.isNotEmpty()) {
                    Text(stringResource(R.string.screenshots_clickHint), Modifier.padding(8.dp))
                }
            }
            items(screenshotsViewModel.screenshots) {
                ImageButton(
                    model = it.painterModel,
                    title = it.name,
                    showDetails = false
                ) {
                    scope.launch {
                        screenshotsViewModel.showScreenshotSheet(it)
                    }
                }
            }
        }
    }

    ScreenshotsSheet(
        screenshot = screenshotsViewModel.screenshotSheetScreeenshot,
        state = screenshotsViewModel.screenshotSheetState,
        onShareRequest = { scope.launch { screenshotsViewModel.shareImportedScreenshot(it, context) } },
        onDeleteRequest = { scope.launch { screenshotsViewModel.deleteImportedScreenshot(it) } }
    )
}