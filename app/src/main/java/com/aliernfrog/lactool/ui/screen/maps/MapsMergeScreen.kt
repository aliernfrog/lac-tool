package com.aliernfrog.lactool.ui.screen.maps

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.laclib.data.LACMapToMerge
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.laclib.MutableMapToMerge
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.AppTopBar
import com.aliernfrog.lactool.ui.component.FloatingActionButton
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveButtonRow
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveRowIcon
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveSection
import com.aliernfrog.lactool.ui.component.maps.MapToMerge
import com.aliernfrog.lactool.ui.component.verticalSegmentedShape
import com.aliernfrog.lactool.ui.dialog.MergeMapDialog
import com.aliernfrog.lactool.ui.theme.AppFABPadding
import com.aliernfrog.lactool.ui.viewmodel.MapsMergeViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun MapsMergeScreen(
    mapsMergeViewModel: MapsMergeViewModel = koinViewModel(),
    onNavigateBackRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    AnimatedContent(mapsMergeViewModel.mapListShown) { showMapList ->
        if (showMapList) MapsListScreen(
            title = stringResource(R.string.mapsMerge_addMap),
            showMultiSelectionOptions = false,
            multiSelectFloatingActionButton = { selectedMaps, clearSelection ->
                FloatingActionButton(
                    icon = Icons.Default.AddLocationAlt,
                    text = stringResource(R.string.maps_merge_short),
                    onClick = { scope.launch {
                        mapsMergeViewModel.addMaps(context, *selectedMaps.toTypedArray())
                        mapsMergeViewModel.mapListShown = false
                        clearSelection()
                    } }
                )
            },
            onBackClick = { mapsMergeViewModel.mapListShown = false },
            onMapPick = { scope.launch {
                mapsMergeViewModel.addMaps(context, it)
                mapsMergeViewModel.mapListShown = false
            } }
        )
        else MergeScreen(
            onNavigateBackRequest = onNavigateBackRequest
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MergeScreen(
    mapsMergeViewModel: MapsMergeViewModel = koinViewModel(),
    onNavigateBackRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    AppScaffold(
        topBar = { scrollBehavior ->
            AppTopBar(
                title = stringResource(R.string.mapsMerge),
                scrollBehavior = scrollBehavior,
                onNavigationClick = {
                    onNavigateBackRequest()
                }
            )
        },
        topAppBarState = mapsMergeViewModel.topAppBarState
    ) {
        Box {
            Column(Modifier.fillMaxSize().verticalScroll(mapsMergeViewModel.scrollState)) {
                PickMapButton {
                    mapsMergeViewModel.mapListShown = true
                }
                MapsList(
                    maps = mapsMergeViewModel.mapMerger.mapsToMerge
                )
                Spacer(Modifier.navigationBarsPadding().height(AppFABPadding))
            }

            HorizontalFloatingToolbar(
                expanded = true,
                floatingActionButton = {
                    FloatingToolbarDefaults.VibrantFloatingActionButton(
                        onClick = {
                            mapsMergeViewModel.mergeMapDialogShown = true
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .systemBarsPadding()
            ) {
                IconButton(
                    onClick = { mapsMergeViewModel.mapListShown = true },
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.mapsMerge_addMap)
                    )
                }

                IconButton(
                    onClick = {
                        mapsMergeViewModel.mapMerger.clearMaps()
                    },
                    enabled = mapsMergeViewModel.mapMerger.mapsToMerge.isNotEmpty(),
                    shapes = IconButtonDefaults.shapes(),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                        disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ClearAll,
                        contentDescription = stringResource(R.string.mapsMerge_clearMaps)
                    )
                }
            }
        }
    }

    if (mapsMergeViewModel.mergeMapDialogShown) MergeMapDialog(
        isMerging = mapsMergeViewModel.isMerging,
        onDismissRequest = { mapsMergeViewModel.mergeMapDialogShown = false },
        onConfirm = { newMapName ->
            scope.launch {
                mapsMergeViewModel.mergeMaps(
                    context = context,
                    newMapName = newMapName
                )
            }
        }
    )
}

@Composable
private fun MapsList(
    maps: List<MutableMapToMerge>
) {
    val baseMap = maps.firstOrNull()
    val mapsToMerge = maps.toList().drop(1)
    AnimatedVisibility(baseMap != null) {
        MapButtonWithActions(
            mapToMerge = baseMap ?: MutableMapToMerge(LACMapToMerge("-", "-")),
            mapIndex = 0,
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, top = 8.dp)
                .verticalSegmentedShape()
        )
    }

    AnimatedVisibility(mapsToMerge.isNotEmpty()) {
        ExpressiveSection(
            title = stringResource(R.string.mapsMerge_mapsToMerge)
        ) {
            mapsToMerge.forEachIndexed { index, map ->
                MapButtonWithActions(
                    mapToMerge = map,
                    mapIndex = index+1,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .verticalSegmentedShape(
                            index = index,
                            totalSize = mapsToMerge.size
                        )
                )
            }
        }
    }
}

@Composable
private fun PickMapButton(
    onClick: () -> Unit
) {
    ExpressiveButtonRow(
        title = stringResource(R.string.mapsMerge_addMap),
        icon = {
            ExpressiveRowIcon(
                painter = rememberVectorPainter(Icons.Default.AddLocationAlt)
            )
        },
        containerColor = MaterialTheme.colorScheme.primary,
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .verticalSegmentedShape()
    )
}

@Composable
private fun MapButtonWithActions(
    mapToMerge: MutableMapToMerge,
    mapIndex: Int,
    modifier: Modifier = Modifier,
    mapsMergeViewModel: MapsMergeViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val isBase = mapIndex == 0
    val expanded = mapsMergeViewModel.optionsExpandedFor == mapIndex

    MapToMerge(
        mapToMerge = mapToMerge,
        isBaseMap = isBase,
        expanded = expanded || isBase,
        onUpdateState = { mapsMergeViewModel.mapMerger.pushMapsState() },
        onMakeBase = { mapsMergeViewModel.makeMapBase(mapIndex, mapToMerge.mapName, context) },
        onRemove = { mapsMergeViewModel.removeMap(mapIndex, mapToMerge.mapName, context) },
        modifier = modifier,
        onClickHeader = {
            if (!isBase) {
                mapsMergeViewModel.optionsExpandedFor = if (expanded) 0
                else mapIndex
            }
        }
    )
}