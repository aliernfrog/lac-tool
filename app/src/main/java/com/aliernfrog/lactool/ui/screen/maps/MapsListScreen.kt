package com.aliernfrog.lactool.ui.screen.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.domain.MapsState
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.impl.mapActions
import com.aliernfrog.lactool.ui.component.SettingsButton
import com.aliernfrog.lactool.ui.viewmodel.MapsListViewModel
import io.github.aliernfrog.pftool_shared.impl.FileWrapper
import io.github.aliernfrog.pftool_shared.ui.screen.maps.MapsListScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun MapsListScreen(
    bottomPadding: Dp,
    title: String = stringResource(R.string.mapsList_pickMap),
    vm: MapsListViewModel = koinViewModel(),
    showMultiSelectionActions: Boolean = true,
    multiSelectFloatingActionButton: @Composable (
        selectedMaps: List<MapFile>, clearSelection: () -> Unit
    ) -> Unit = { _, _ -> },
    onNavigateSettingsRequest: (() -> Unit)? = null,
    onBackClick: (() -> Unit)?,
    onMapPick: (MapFile) -> Unit
) {
    val mapsState = koinInject<MapsState>()

    @Suppress("UNCHECKED_CAST")
    MapsListScreen(
        title = title,
        mapsListSegments = vm.availableSegments,
        mapActions = mapActions,
        listViewOptions = vm.prefs.mapsListOptions,
        showThumbnailsInList = vm.prefs.showMapThumbnailsInList.value,
        dimList = mapsState.addMapMenuExpanded,
        showMultiSelectionActions = showMultiSelectionActions,
        extraBottomPadding = bottomPadding,
        multiSelectFloatingActionButton = { selectedMaps, clearSelection ->
            multiSelectFloatingActionButton(selectedMaps as List<MapFile>, clearSelection)
        },
        settingsButton = onNavigateSettingsRequest?.let { {
            SettingsButton(onClick = it)
        } },
        onRemoveDimRequest = { mapsState.addMapMenuExpanded = false },
        onBackClick = onBackClick,
        onMapPick = {
            onMapPick(when (it) {
                is MapFile -> it
                is FileWrapper -> MapFile(it)
                else -> MapFile(FileWrapper(it))
            })
        }
    )
}