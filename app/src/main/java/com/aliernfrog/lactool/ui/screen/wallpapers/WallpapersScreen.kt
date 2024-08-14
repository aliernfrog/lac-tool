package com.aliernfrog.lactool.ui.screen.wallpapers

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.HideImage
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.FileWrapper
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.AppTopBar
import com.aliernfrog.lactool.ui.component.ButtonIcon
import com.aliernfrog.lactool.ui.component.ColumnRounded
import com.aliernfrog.lactool.ui.component.ErrorWithIcon
import com.aliernfrog.lactool.ui.component.FadeVisibility
import com.aliernfrog.lactool.ui.component.ImageButton
import com.aliernfrog.lactool.ui.component.SettingsButton
import com.aliernfrog.lactool.ui.component.form.RoundedButtonRow
import com.aliernfrog.lactool.ui.sheet.WallpaperSheet
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.ui.viewmodel.WallpapersViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpapersScreen(
    wallpapersViewModel: WallpapersViewModel = koinViewModel(),
    onNavigateSettingsRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        wallpapersViewModel.getWallpapersFile(context)
        wallpapersViewModel.fetchImportedWallpapers()
    }

    AppScaffold(
        topBar = { scrollBehavior ->
            AppTopBar(
                title = stringResource(R.string.wallpapers),
                scrollBehavior = scrollBehavior,
                actions = {
                    SettingsButton(onClick = onNavigateSettingsRequest)
                }
            )
        },
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
                PickedWallpaper(
                    pickedWallpaper = wallpapersViewModel.pickedWallpaper,
                    wallpaperName = wallpapersViewModel.wallpaperNameInputRaw,
                    onWallpaperNameChange = {
                        wallpapersViewModel.wallpaperNameInputRaw = it
                    }
                ) {
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
                    title = it.nameWithoutExtension,
                    description = stringResource(R.string.wallpapers_list_clickToViewActions)
                ) {
                    scope.launch {
                        wallpapersViewModel.showWallpaperSheet(it)
                    }
                }
            }
        }
    }

    WallpaperSheet(
        wallpaper = wallpapersViewModel.wallpaperSheetWallpaper,
        wallpapersPath = wallpapersViewModel.wallpapersDir,
        state = wallpapersViewModel.wallpaperSheetState,
        topToastState = wallpapersViewModel.topToastState,
        onShareRequest = { scope.launch { wallpapersViewModel.shareImportedWallpaper(it, context) } },
        onDeleteRequest = { scope.launch { wallpapersViewModel.deleteImportedWallpaper(it) } }
    )
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
    pickedWallpaper: FileWrapper?,
    wallpaperName: String,
    onWallpaperNameChange: (String) -> Unit,
    onImport: () -> Unit
) {
    FadeVisibility(pickedWallpaper != null) {
        val originalName = pickedWallpaper?.nameWithoutExtension ?: ""
        ColumnRounded(title = stringResource(R.string.wallpapers_chosen)) {
            AsyncImage(
                model = pickedWallpaper?.painterModel,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().padding(8.dp).clip(AppComponentShape),
                contentScale = ContentScale.Crop
            )
            OutlinedTextField(
                value = wallpaperName,
                onValueChange = onWallpaperNameChange,
                label = {
                    Text(stringResource(R.string.wallpapers_chosen_name))
                },
                placeholder = {
                    Text(originalName)
                },
                trailingIcon = {
                    Crossfade(wallpaperName != originalName) { enabled ->
                        IconButton(
                            onClick = { onWallpaperNameChange(pickedWallpaper?.nameWithoutExtension ?: "") },
                            enabled = enabled
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = stringResource(R.string.wallpapers_chosen_name_reset)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Button(
                    onClick = onImport
                ) {
                    ButtonIcon(rememberVectorPainter(Icons.Rounded.Download))
                    Text(stringResource(R.string.wallpapers_chosen_import))
                }
            }
        }
    }
}