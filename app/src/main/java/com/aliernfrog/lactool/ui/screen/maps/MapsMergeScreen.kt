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
import androidx.compose.material3.FloatingToolbarDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.laclib.data.LACMapToMerge
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.laclib.MutableMapToMerge
import com.aliernfrog.lactool.ui.component.maps.MapToMerge
import com.aliernfrog.lactool.ui.dialog.MergeMapDialog
import com.aliernfrog.lactool.ui.viewmodel.MapsMergeViewModel
import io.github.aliernfrog.shared.ui.component.AppScaffold
import io.github.aliernfrog.shared.ui.component.AppTopBar
import io.github.aliernfrog.shared.ui.component.ButtonIcon
import io.github.aliernfrog.shared.ui.component.ErrorWithIcon
import io.github.aliernfrog.shared.ui.component.FadeVisibility
import io.github.aliernfrog.shared.ui.component.FloatingActionButton
import io.github.aliernfrog.shared.ui.component.expressive.ExpressiveSection
import io.github.aliernfrog.shared.ui.component.util.ScrollAccessibilityListener
import io.github.aliernfrog.shared.ui.component.verticalSegmentedShape
import io.github.aliernfrog.shared.ui.theme.AppFABPadding
import kotlinx.coroutines.launch

@Composable
fun MapsMergeScreen(
    vm: MapsMergeViewModel,
    onNavigateBackRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    AnimatedContent(vm.mapListShown) { showMapList ->
        if (showMapList) MapsListScreen(
            title = stringResource(R.string.mapsMerge_addMap),
            showMultiSelectionActions = false,
            multiSelectFloatingActionButton = { selectedMaps, clearSelection ->
                FloatingActionButton(
                    icon = Icons.Default.AddLocationAlt,
                    text = stringResource(R.string.maps_merge_short),
                    modifier = Modifier.navigationBarsPadding(),
                    onClick = { scope.launch {
                        vm.addMaps(context, *selectedMaps.toTypedArray())
                        vm.mapListShown = false
                        clearSelection()
                    } }
                )
            },
            onBackClick = { vm.mapListShown = false },
            onMapPick = { scope.launch {
                vm.addMaps(context, it)
                vm.mapListShown = false
            } }
        )
        else MergeScreen(
            vm = vm,
            onNavigateBackRequest = onNavigateBackRequest
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MergeScreen(
    vm: MapsMergeViewModel,
    onNavigateBackRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showToolbarLabels by remember { mutableStateOf(true) }

    ScrollAccessibilityListener(
        scrollState = vm.scrollState,
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
        topAppBarState = vm.topAppBarState
    ) {
        Box {
            Column(Modifier.fillMaxSize().verticalScroll(vm.scrollState)) {
                MapsList(
                    vm = vm,
                    maps = vm.mapMerger.mapsToMerge,
                    onPickMapRequest = { vm.mapListShown = true }
                )
                Spacer(Modifier.navigationBarsPadding().height(AppFABPadding))
            }

            HorizontalFloatingToolbar(
                expanded = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .systemBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = FloatingToolbarDefaults.ContainerShape
                    )
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

                    Crossfade(vm.mapMerger.mapsToMerge.isNotEmpty()) { enabled ->
                        TextButton(
                            onClick = { vm.mapMerger.clearMaps() },
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
                        onClick = { vm.mapListShown = true },
                        shapes = ButtonDefaults.shapes()
                    ) {
                        ButtonContent(
                            icon = Icons.Default.Add,
                            label = stringResource(R.string.mapsMerge_addMap),
                            isMain = true
                        )
                    }

                    Spacer(Modifier.width(4.dp))

                    Crossfade(vm.hasEnoughMaps) { enabled ->
                        TextButton(
                            onClick = { vm.mergeMapDialogShown = true },
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

    if (vm.mergeMapDialogShown) MergeMapDialog(
        isMerging = vm.isMerging,
        onDismissRequest = { vm.mergeMapDialogShown = false },
        onConfirm = { newMapName ->
            scope.launch {
                vm.mergeMaps(
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
    vm: MapsMergeViewModel,
    maps: List<MutableMapToMerge>,
    onPickMapRequest: () -> Unit
) {
    val baseMap = maps.firstOrNull()
    val mapsToMerge = maps.toList().drop(1)

    FadeVisibility(maps.isEmpty()) {
        ErrorWithIcon(
            description = stringResource(R.string.mapsMerge_noMaps),
            icon = rememberVectorPainter(Icons.Default.AddLocationAlt),
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
                vm = vm,
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
                    vm = vm,
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
    vm: MapsMergeViewModel,
    mapToMerge: MutableMapToMerge,
    mapIndex: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBase = mapIndex == 0
    val expanded = vm.optionsExpandedFor == mapIndex

    MapToMerge(
        mapToMerge = mapToMerge,
        isBaseMap = isBase,
        expanded = expanded || isBase,
        onUpdateState = { vm.mapMerger.pushMapsState() },
        onMakeBase = { vm.makeMapBase(mapIndex, mapToMerge.mapName, context) },
        onRemove = { vm.removeMap(mapIndex, mapToMerge.mapName, context) },
        modifier = modifier,
        onClickHeader = {
            if (!isBase) {
                vm.optionsExpandedFor = if (expanded) 0
                else mapIndex
            }
        }
    )
}