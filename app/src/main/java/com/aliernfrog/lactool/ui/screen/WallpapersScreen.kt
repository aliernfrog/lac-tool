package com.aliernfrog.lactool.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.WallpapersListItem
import com.aliernfrog.lactool.state.WallpapersState
import com.aliernfrog.lactool.ui.component.ButtonRounded
import com.aliernfrog.lactool.ui.component.ColumnRounded

@Composable
fun WallpapersScreen(wallpapersState: WallpapersState) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { wallpapersState.getWallpapersFile(context); wallpapersState.getImportedWallpapers() }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = wallpapersState.lazyListState
    ) {
        item {
            PickImageButton()
        }
        items(wallpapersState.importedWallpapers.value) {
            Wallpaper(it)
        }
    }
}

@Composable
private fun PickImageButton() {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {}
    )
    ButtonRounded(
        title = stringResource(R.string.wallpapers_pickImage),
        painter = rememberVectorPainter(Icons.Rounded.Image),
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        launcher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
}

@Composable
private fun Wallpaper(wallpaper: WallpapersListItem) {
    ColumnRounded {
        AsyncImage(
            model = wallpaper.painterModel,
            contentDescription = null
        )
    }
}