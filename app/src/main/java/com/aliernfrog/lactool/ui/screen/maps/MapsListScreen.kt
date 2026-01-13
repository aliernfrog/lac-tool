package com.aliernfrog.lactool.ui.screen.maps

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.impl.mapActions
import com.aliernfrog.lactool.ui.component.SettingsButton
import com.aliernfrog.lactool.ui.viewmodel.MapsListViewModel
import io.github.aliernfrog.pftool_shared.impl.FileWrapper
import io.github.aliernfrog.pftool_shared.ui.screen.maps.MapsListScreen
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MapsListScreen(
    title: String = stringResource(R.string.mapsList_pickMap),
    vm: MapsListViewModel = koinViewModel(),
    showMultiSelectionOptions: Boolean = true,
    multiSelectFloatingActionButton: @Composable (
        selectedMaps: List<MapFile>, clearSelection: () -> Unit
    ) -> Unit = { _, _ -> },
    onNavigateSettingsRequest: (() -> Unit)? = null,
    onBackClick: (() -> Unit)?,
    onMapPick: (MapFile) -> Unit
) {
    @Suppress("UNCHECKED_CAST")
    MapsListScreen(
        title = title,
        mapsListSegments = vm.availableSegments,
        mapActions = mapActions,
        listViewOptions = vm.prefs.mapsListOptions,
        showThumbnailsInList = vm.prefs.showMapThumbnailsInList.value,
        showMultiSelectionOptions = showMultiSelectionOptions,
        multiSelectFloatingActionButton = { selectedMaps, clearSelection ->
            multiSelectFloatingActionButton(selectedMaps as List<MapFile>, clearSelection)
        },
        settingsButton = onNavigateSettingsRequest?.let { {
            SettingsButton(onClick = it)
        } },
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