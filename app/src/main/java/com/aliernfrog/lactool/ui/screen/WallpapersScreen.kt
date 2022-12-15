package com.aliernfrog.lactool.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aliernfrog.lactool.AppComposableShape
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.WallpapersListItem
import com.aliernfrog.lactool.state.WallpapersState
import com.aliernfrog.lactool.ui.component.ButtonRounded
import com.aliernfrog.lactool.ui.component.ColumnRounded
import kotlinx.coroutines.launch

@Composable
fun WallpapersScreen(wallpapersState: WallpapersState) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { wallpapersState.getWallpapersFile(context); wallpapersState.getImportedWallpapers() }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = wallpapersState.lazyListState
    ) {
        item {
            PickImageButton(wallpapersState)
            ChosenWallpaper(wallpapersState)
        }
        items(wallpapersState.importedWallpapers.value) {
            Wallpaper(it)
        }
    }
}

@Composable
private fun PickImageButton(wallpapersState: WallpapersState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) scope.launch { wallpapersState.setChosenWallpaper(uri, context) }
        }
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
private fun ChosenWallpaper(wallpapersState: WallpapersState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    AnimatedVisibility(
        visible = wallpapersState.chosenWallpaper.value != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        ColumnRounded(title = stringResource(R.string.wallpapers_chosenWallpaper)) {
            AsyncImage(
                model = wallpapersState.chosenWallpaper.value?.painterModel,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().padding(8.dp).clip(AppComposableShape),
                contentScale = ContentScale.Crop
            )
            ButtonRounded(
                title = stringResource(R.string.wallpapers_chosen_import),
                painter = rememberVectorPainter(Icons.Rounded.Download),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                scope.launch { wallpapersState.importChosenWallpaper(context) }
            }
        }
    }
}

@Composable
private fun Wallpaper(wallpaper: WallpapersListItem) {
    ColumnRounded {
        AsyncImage(
            model = wallpaper.painterModel,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().padding(8.dp).clip(AppComposableShape),
            contentScale = ContentScale.Crop
        )
    }
}