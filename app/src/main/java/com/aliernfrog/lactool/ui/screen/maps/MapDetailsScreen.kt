package com.aliernfrog.lactool.ui.screen.maps

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.enum.MapAction
import com.aliernfrog.lactool.impl.MapActionArguments
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.AppSmallTopBar
import com.aliernfrog.lactool.ui.component.SettingsButton
import com.aliernfrog.lactool.ui.component.maps.GridMapItem
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel
import io.github.aliernfrog.pftool_shared.ui.component.ButtonIcon
import io.github.aliernfrog.pftool_shared.ui.component.FadeVisibility
import io.github.aliernfrog.pftool_shared.ui.component.VerticalSegmentor
import io.github.aliernfrog.pftool_shared.ui.component.expressive.ExpressiveButtonRow
import io.github.aliernfrog.pftool_shared.ui.component.expressive.ExpressiveRowIcon
import io.github.aliernfrog.pftool_shared.ui.theme.AppComponentShape
import io.github.aliernfrog.pftool_shared.util.extension.clickableWithColor
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapDetailsScreen(
    map: MapFile,
    mapsViewModel: MapsViewModel = koinViewModel(),
    onNavigateSettingsRequest: () -> Unit,
    onNavigateBackRequest: (() -> Unit)?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val mapNameEdit = rememberSaveable {
        mutableStateOf(map.name.replace("\n",""))
    }

    val isSameName = mapNameEdit.value.let {
        it.isBlank() || it == map.name
    }

    AppScaffold(
        topBar = { scrollBehavior ->
            AppSmallTopBar(
                title = map.name,
                scrollBehavior = scrollBehavior,
                onNavigationClick = onNavigateBackRequest,
                actions = {
                    SettingsButton(onClick = onNavigateSettingsRequest)
                }
            )
        },
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            VerticalSegmentor({
                MapCard(
                    chosenMap = map,
                    showMapThumbnail = mapsViewModel.prefs.showChosenMapThumbnail.value,
                    onViewThumbnailRequest = {
                        mapsViewModel.openMapThumbnailViewer(map)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .heightIn(min = 150.dp, max = 150.dp)
                )
            }, {
                TextField(
                    value = mapNameEdit.value,
                    onValueChange = {
                        mapNameEdit.value = it.replace("\n", "")
                    },
                    label = { Text(stringResource(R.string.maps_mapName)) },
                    placeholder = { Text(map.name) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.TextFields,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }, modifier = Modifier.padding(
                start = 12.dp,
                end = 12.dp,
                bottom = 8.dp
            ))

            FadeVisibility(visible = mapsViewModel.prefs.showMapNameFieldGuide.value) {
                OutlinedCard(
                    onClick = { mapsViewModel.prefs.showMapNameFieldGuide.value = false },
                    shape = AppComponentShape,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 4.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        )
                    ) {
                        Icon(Icons.Rounded.TipsAndUpdates, contentDescription = null)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.maps_mapName_guide),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = stringResource(R.string.action_tapToDismiss),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.alpha(0.7f)
                            )
                        }
                    }
                }
            }

            Crossfade(
                targetState = MapAction.RENAME.availableFor(map) && !isSameName
            ) { buttonsEnabled ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 12.dp,
                            vertical = 4.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = { scope.launch {
                            MapAction.DUPLICATE.execute(context, map, args = MapActionArguments(mapName = mapNameEdit.value))
                        } },
                        shapes = ButtonDefaults.shapes(),
                        enabled = buttonsEnabled
                    ) {
                        ButtonIcon(rememberVectorPainter(Icons.Default.FileCopy))
                        Text(stringResource(R.string.maps_duplicate))
                    }
                    Button(
                        onClick = { scope.launch {
                            MapAction.RENAME.execute(context, map, args = MapActionArguments(mapName = mapNameEdit.value))
                        } },
                        shapes = ButtonDefaults.shapes(),
                        enabled = buttonsEnabled
                    ) {
                        ButtonIcon(rememberVectorPainter(Icons.Default.Edit))
                        Text(stringResource(R.string.maps_rename))
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).alpha(0.7f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            val actions: List<@Composable () -> Unit> = MapAction.entries.filter { action ->
                action != MapAction.RENAME && action != MapAction.DUPLICATE
            }.map { action -> {
                FadeVisibility(visible = action.availableFor(map)) {
                    ExpressiveButtonRow(
                        title = stringResource(action.longLabel),
                        description = action.description?.let { stringResource(it) },
                        icon = {
                            ExpressiveRowIcon(
                                painter = rememberVectorPainter(action.icon),
                                containerColor = if (action.destructive) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primaryContainer
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = if (action.destructive) MaterialTheme.colorScheme.error
                        else contentColorFor(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) { scope.launch {
                        action.execute(context = context, map, args = MapActionArguments(mapName = mapNameEdit.value))
                    } }
                }
            } }

            VerticalSegmentor(
                *actions.toTypedArray(),
                dynamic = true,
                modifier = Modifier.padding(
                    vertical = 8.dp,
                    horizontal = 12.dp
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MapCard(
    chosenMap: MapFile,
    showMapThumbnail: Boolean,
    onViewThumbnailRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier.clickableWithColor(
            color = MaterialTheme.colorScheme.onSurface,
            onClick = onViewThumbnailRequest
        )
    ) {
        GridMapItem(
            map = chosenMap,
            selected = null,
            showMapThumbnail = showMapThumbnail,
            onSelectedChange = {},
            onLongClick = {},
            onClick = onViewThumbnailRequest,
            aspectRatio = null,
            placeholderIcon = if (chosenMap.thumbnailModel != null) Icons.Outlined.Image else Icons.Outlined.HideImage,
            modifier = modifier
        )
        if (chosenMap.thumbnailModel != null && showMapThumbnail) FilledIconButton(
            onClick = onViewThumbnailRequest,
            shapes = IconButtonDefaults.shapes(),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
            ),
            modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = stringResource(R.string.maps_thumbnail)
            )
        }
    }
}