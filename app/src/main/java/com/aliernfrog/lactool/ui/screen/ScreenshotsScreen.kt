package com.aliernfrog.lactool.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.state.ScreenshotsState
import com.aliernfrog.lactool.ui.component.ColumnRounded
import com.aliernfrog.lactool.ui.component.ImageButton
import kotlinx.coroutines.launch

@Composable
fun ScreenshotScreen(screenshotsState: ScreenshotsState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { screenshotsState.getScreenshotsFile(context); screenshotsState.getImportedScreenshots() }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = screenshotsState.lazyListState
    ) {
        item {
            AnimatedVisibility(
                visible = screenshotsState.importedScreenshots.value.isEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ColumnRounded(color = MaterialTheme.colorScheme.error) {
                    Text(stringResource(R.string.screenshots_noScreenshots), color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                }
            }
            AnimatedVisibility(
                visible = screenshotsState.importedScreenshots.value.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Text(stringResource(R.string.screenshots_clickHint), Modifier.padding(8.dp))
            }
        }
        items(screenshotsState.importedScreenshots.value) {
            ImageButton(
                model = it.painterModel,
                title = it.name,
                showDetails = false
            ) {
                scope.launch { screenshotsState.showScreenshotSheet(it) }
            }
        }
    }
}