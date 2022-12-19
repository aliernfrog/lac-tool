package com.aliernfrog.lactool.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.state.ScreenshotsState
import com.aliernfrog.lactool.ui.component.ImageButton

@Composable
fun ScreenshotScreen(screenshotsState: ScreenshotsState) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { screenshotsState.getScreenshotsFile(context); screenshotsState.getImportedScreenshots() }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = screenshotsState.lazyListState
    ) {
        items(screenshotsState.importedScreenshots.value) {
            ImageButton(
                model = it.painterModel,
                title = it.name,
                description = stringResource(R.string.wallpapers_list_clickToViewActions)
            ) {
                // TODO
            }
        }
    }
}