package com.aliernfrog.lactool.ui.screen.wallpapers

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Restore
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
import coil.compose.AsyncImage
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.enum.ListStyle
import com.aliernfrog.lactool.impl.FileWrapper
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.AppTopBar
import com.aliernfrog.lactool.ui.component.ButtonIcon
import com.aliernfrog.lactool.ui.component.ColumnRounded
import com.aliernfrog.lactool.ui.component.ErrorWithIcon
import com.aliernfrog.lactool.ui.component.FadeVisibility
import com.aliernfrog.lactool.ui.component.LazyAdaptiveVerticalGrid
import com.aliernfrog.lactool.ui.component.ImageButton
import com.aliernfrog.lactool.ui.component.ImageButtonOverlay
import com.aliernfrog.lactool.ui.component.ListViewOptionsDropdown
import com.aliernfrog.lactool.ui.component.SettingsButton
import com.aliernfrog.lactool.ui.component.form.RoundedButtonRow
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
    val listStyle = ListStyle.entries[wallpapersViewModel.prefs.wallpapersListStyle.value]

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
        @Composable
        fun WallpaperButton(
            wallpaper: FileWrapper,
            modifier: Modifier = Modifier,
            contentScale: ContentScale = ContentScale.FillWidth,
            showOverlay: Boolean = true
        ) {
            ImageButton(
                model = wallpaper.painterModel,
                contentScale = contentScale,
                onClick = {
                    wallpapersViewModel.openWallpaperOptions(wallpaper)
                },
                modifier = modifier
                    .padding(8.dp)
                    .clip(AppComponentShape)
            ) {
                if (showOverlay) ImageButtonOverlay(
                    title = if (wallpaper == wallpapersViewModel.activeWallpaper) stringResource(R.string.wallpapers_list_active)
                    else wallpaper.nameWithoutExtension,
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {}
            }
        }

        @Composable
        fun ListHeader() {
            Header(
                wallpaperButton = {
                    WallpaperButton(it)
                }
            )
        }

        AnimatedContent(targetState = listStyle) { style ->
            when (style) {
                ListStyle.LIST -> LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        ListHeader()
                    }
                    items(wallpapersViewModel.wallpapersToShow) {
                        WallpaperButton(
                            wallpaper = it,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
                ListStyle.GRID -> LazyAdaptiveVerticalGrid(
                    modifier = Modifier.fillMaxSize()
                ) { maxLineSpan: Int ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ListHeader()
                    }
                    items(wallpapersViewModel.wallpapersToShow) {
                        WallpaperButton(
                            wallpaper = it,
                            contentScale = ContentScale.Crop,
                            showOverlay = false,
                            modifier = Modifier
                                .animateItem()
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(
    wallpapersViewModel: WallpapersViewModel = koinViewModel(),
    wallpaperButton: @Composable (wallpaper: FileWrapper) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hasAtLeastOneWallpaper = wallpapersViewModel.wallpapersToShow.isNotEmpty()
            || wallpapersViewModel.activeWallpaper != null
    var listOptionsExpanded by remember { mutableStateOf(false) }

    Column {
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
        FadeVisibility(hasAtLeastOneWallpaper) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.wallpapers_clickHint),
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
                        sortingPref = wallpapersViewModel.prefs.wallpapersListSorting,
                        sortingReversedPref = wallpapersViewModel.prefs.wallpapersListSortingReversed,
                        stylePref = wallpapersViewModel.prefs.wallpapersListStyle
                    )
                }
            }
        }
        ErrorWithIcon(
            error = stringResource(R.string.wallpapers_noWallpapers),
            painter = rememberVectorPainter(Icons.Rounded.HideImage),
            visible = !hasAtLeastOneWallpaper,
            modifier = Modifier.fillMaxWidth()
        )
        wallpapersViewModel.activeWallpaper?.let {
            wallpaperButton(it)
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
                    ButtonIcon(rememberVectorPainter(Icons.Default.Download))
                    Text(stringResource(R.string.wallpapers_chosen_import))
                }
            }
        }
    }
}