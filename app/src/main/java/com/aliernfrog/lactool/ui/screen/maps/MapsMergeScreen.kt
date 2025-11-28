package com.aliernfrog.lactool.ui.screen.maps

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.laclib.data.LACMapToMerge
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.laclib.MutableMapToMerge
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.AppTopBar
import com.aliernfrog.lactool.ui.component.maps.MapToMerge
import com.aliernfrog.lactool.ui.component.util.ScrollAccessibilityListener
import com.aliernfrog.lactool.ui.dialog.MergeMapDialog
import com.aliernfrog.lactool.ui.viewmodel.MapsMergeViewModel
import io.github.aliernfrog.pftool_shared.ui.component.ButtonIcon
import io.github.aliernfrog.pftool_shared.ui.component.ErrorWithIcon
import io.github.aliernfrog.pftool_shared.ui.component.FadeVisibility
import io.github.aliernfrog.pftool_shared.ui.component.FloatingActionButton
import io.github.aliernfrog.pftool_shared.ui.component.expressive.ExpressiveSection
import io.github.aliernfrog.pftool_shared.ui.component.verticalSegmentedShape
import io.github.aliernfrog.pftool_shared.ui.theme.AppFABPadding
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
                    modifier = Modifier.navigationBarsPadding(),
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

    var showToolbarLabels by remember { mutableStateOf(true) }

    ScrollAccessibilityListener(
        scrollState = mapsMergeViewModel.scrollState,
        onShowLabelsStateChange = { showToolbarLabels = it }
    )

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
                MapsList(
                    maps = mapsMergeViewModel.mapMerger.mapsToMerge,
                    onPickMapRequest = { mapsMergeViewModel.mapListShown = true }
                )
                Spacer(Modifier.navigationBarsPadding().height(AppFABPadding))
            }

            HorizontalFloatingToolbar(
                expanded = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .systemBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                @Composable
                fun ToolbarContent(showLabelsOverride: Boolean?) {
                    @Composable
                    fun ButtonContent(icon: ImageVector, label: String, isMain: Boolean = false) {
                        Icon(
                            icon, label
                        )

                        if (showLabelsOverride != false || isMain) {
                            AnimatedVisibility(showToolbarLabels || showLabelsOverride == true) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                        }
                    }

                    Crossfade(mapsMergeViewModel.mapMerger.mapsToMerge.isNotEmpty()) { enabled ->
                        TextButton(
                            onClick = { mapsMergeViewModel.mapMerger.clearMaps() },
                            shapes = ButtonDefaults.shapes(),
                            enabled = enabled
                        ) {
                            ButtonContent(
                                icon = Icons.Default.ClearAll,
                                label = stringResource(R.string.mapsMerge_clearMaps)
                            )
                        }
                    }

                    Spacer(Modifier.width(4.dp))

                    FilledTonalButton(
                        onClick = { mapsMergeViewModel.mapListShown = true },
                        shapes = ButtonDefaults.shapes()
                    ) {
                        ButtonContent(
                            icon = Icons.Default.Add,
                            label = stringResource(R.string.mapsMerge_addMap),
                            isMain = true
                        )
                    }

                    Spacer(Modifier.width(4.dp))

                    Crossfade(mapsMergeViewModel.hasEnoughMaps) { enabled ->
                        TextButton(
                            onClick = { mapsMergeViewModel.mergeMapDialogShown = true },
                            shapes = ButtonDefaults.shapes(),
                            enabled = enabled
                        ) {
                            ButtonContent(
                                icon = Icons.Default.Save,
                                label = stringResource(R.string.mapsMerge_merge)
                            )
                        }
                    }
                }

                SubcomposeLayout { constraints ->
                    val fullContentPlaceables = subcompose("fullContent") {
                        ToolbarContent(showLabelsOverride = true)
                    }.map { it.measure(constraints) }

                    val fullWidth = fullContentPlaceables.sumOf { it.width }

                    val placeables = (if (fullWidth <= constraints.maxWidth) subcompose("dynamic") {
                        ToolbarContent(showLabelsOverride = null)
                    } else subcompose("noLabels") {
                        ToolbarContent(showLabelsOverride = false)
                    }).map { it.measure(constraints) }

                    val width = placeables.sumOf { it.width }
                    val height = placeables.maxOfOrNull { it.height } ?: 0

                    layout(width, height) {
                        var xPos = 0
                        placeables.forEach {
                            it.placeRelative(xPos, 0)
                            xPos += it.width
                        }
                    }
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MapsList(
    maps: List<MutableMapToMerge>,
    onPickMapRequest: () -> Unit
) {
    val baseMap = maps.firstOrNull()
    val mapsToMerge = maps.toList().drop(1)

    FadeVisibility(maps.isEmpty()) {
        ErrorWithIcon(
            error = stringResource(R.string.mapsMerge_noMaps),
            painter = rememberVectorPainter(Icons.Default.AddLocationAlt),
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            button = {
                Button(
                    onClick = onPickMapRequest,
                    shapes = ButtonDefaults.shapes()
                ) {
                    ButtonIcon(rememberVectorPainter(Icons.Default.Add))
                    Text(stringResource(R.string.mapsMerge_addMap))
                }
            }
        )
    }

    FadeVisibility(baseMap != null) {
        ExpressiveSection(
            title = stringResource(R.string.mapsMerge_base)
        ) {
            MapButtonWithActions(
                mapToMerge = baseMap ?: MutableMapToMerge(LACMapToMerge("-", "-")),
                mapIndex = 0,
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp)
                    .verticalSegmentedShape()
            )
        }
    }

    FadeVisibility(mapsToMerge.isNotEmpty()) {
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