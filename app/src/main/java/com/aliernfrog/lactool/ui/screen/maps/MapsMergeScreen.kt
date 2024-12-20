package com.aliernfrog.lactool.ui.screen.maps

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.aliernfrog.laclib.data.LACMapToMerge
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.laclib.MutableMapToMerge
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.AppTopBar
import com.aliernfrog.lactool.ui.component.FloatingActionButton
import com.aliernfrog.lactool.ui.component.form.FormSection
import com.aliernfrog.lactool.ui.component.form.RoundedButtonRow
import com.aliernfrog.lactool.ui.component.maps.MapToMerge
import com.aliernfrog.lactool.ui.dialog.MergeMapDialog
import com.aliernfrog.lactool.ui.theme.AppFABHeight
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

@OptIn(ExperimentalMaterial3Api::class)
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
        topAppBarState = mapsMergeViewModel.topAppBarState,
        floatingActionButton = {
            AnimatedVisibility(
                visible = mapsMergeViewModel.hasEnoughMaps && !mapsMergeViewModel.mergeMapDialogShown,
                modifier = Modifier.systemBarsPadding(),
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    icon = Icons.Default.Build,
                    text = stringResource(R.string.mapsMerge_merge),
                    onClick = { mapsMergeViewModel.mergeMapDialogShown = true }
                )
            }
        }
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(mapsMergeViewModel.scrollState)) {
            PickMapButton {
                mapsMergeViewModel.mapListShown = true
            }
            MapsList(
                maps = mapsMergeViewModel.mapMerger.mapsToMerge
            )
            Spacer(Modifier.navigationBarsPadding().height(AppFABHeight))
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
            mapIndex = 0
        )
    }
    AnimatedVisibility(mapsToMerge.isNotEmpty()) {
        FormSection(
            title = stringResource(R.string.mapsMerge_mapsToMerge),
            topDivider = true,
            bottomDivider = false
        ) {
            mapsToMerge.forEachIndexed { index, map ->
                MapButtonWithActions(
                    mapToMerge = map,
                    mapIndex = index+1,
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

@Composable
private fun PickMapButton(
    onClick: () -> Unit
) {
    RoundedButtonRow(
        title = stringResource(R.string.mapsMerge_addMap),
        painter = rememberVectorPainter(Icons.Rounded.AddLocationAlt),
        containerColor = MaterialTheme.colorScheme.primary,
        onClick = onClick
    )
}

@Composable
private fun MapButtonWithActions(
    mapsMergeViewModel: MapsMergeViewModel = koinViewModel(),
    mapToMerge: MutableMapToMerge,
    mapIndex: Int,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val context = LocalContext.current
    val isBase = mapIndex == 0
    val expanded = mapsMergeViewModel.optionsExpandedFor == mapIndex
    MapToMerge(
        mapToMerge = mapToMerge,
        isBaseMap = isBase,
        expanded = expanded || isBase,
        containerColor = containerColor,
        onUpdateState = { mapsMergeViewModel.mapMerger.pushMapsState() },
        onMakeBase = { mapsMergeViewModel.makeMapBase(mapIndex, mapToMerge.mapName, context) },
        onRemove = { mapsMergeViewModel.removeMap(mapIndex, mapToMerge.mapName, context) },
        onClickHeader = {
            if (!isBase) {
                mapsMergeViewModel.optionsExpandedFor = if (expanded) 0
                else mapIndex
            }
        }
    )
}