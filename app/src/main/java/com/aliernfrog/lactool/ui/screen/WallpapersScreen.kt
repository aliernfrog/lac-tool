package com.aliernfrog.lactool.ui.screen

import android.net.Uri
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
import androidx.compose.material.icons.rounded.HideImage
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.ImageFile
import com.aliernfrog.lactool.ui.component.*
import com.aliernfrog.lactool.ui.component.form.RoundedButtonRow
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.ui.viewmodel.WallpapersViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpapersScreen(
    wallpapersViewModel: WallpapersViewModel = getViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        wallpapersViewModel.getWallpapersFile(context)
        wallpapersViewModel.fetchImportedWallpapers()
    }
    AppScaffold(
        title = stringResource(R.string.wallpapers),
        topAppBarState = wallpapersViewModel.topAppBarState
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = wallpapersViewModel.lazyListState
        ) {
            item {
                PickImageButton {
                    scope.launch {
                        wallpapersViewModel.setPickedWallpaper(it, context)
                    }
                }
                PickedWallpaper(wallpapersViewModel.pickedWallpaper) {
                    scope.launch {
                        wallpapersViewModel.importPickedWallpaper(context)
                    }
                }
                ErrorWithIcon(
                    error = stringResource(R.string.wallpapers_noWallpapers),
                    painter = rememberVectorPainter(Icons.Rounded.HideImage),
                    visible = wallpapersViewModel.importedWallpapers.isEmpty()
                )
            }
            items(wallpapersViewModel.importedWallpapers) {
                ImageButton(
                    model = it.painterModel,
                    title = it.name,
                    description = stringResource(R.string.wallpapers_list_clickToViewActions)
                ) {
                    scope.launch {
                        wallpapersViewModel.showWallpaperSheet(it)
                    }
                }
            }
        }
    }
}

@Composable
private fun PickImageButton(
    onPickUri: (uri: Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) onPickUri(uri)
        }
    )
    RoundedButtonRow(
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
private fun PickedWallpaper(
    pickedWallpaper: ImageFile?,
    onImport: () -> Unit
) {
    FadeVisibility(pickedWallpaper != null) {
        ColumnRounded(title = stringResource(R.string.wallpapers_chosenWallpaper)) {
            AsyncImage(
                model = pickedWallpaper?.painterModel,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().padding(8.dp).clip(AppComponentShape),
                contentScale = ContentScale.Crop
            )
            RoundedButtonRow(
                title = stringResource(R.string.wallpapers_chosen_import),
                painter = rememberVectorPainter(Icons.Rounded.Download),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                onImport()
            }
        }
    }
}