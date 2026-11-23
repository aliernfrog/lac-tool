package com.aliernfrog.lactool.ui.screen.maps

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.impl.mapActions
import com.aliernfrog.lactool.ui.component.SettingsButton
import com.aliernfrog.lactool.ui.viewmodel.MapsListViewModel
import io.github.aliernfrog.pftool_shared.impl.FileWrapper
import io.github.aliernfrog.pftool_shared.ui.screen.maps.MapsListScreen
import io.github.aliernfrog.shared.ui.component.util.AnimatedContentShadowWorkaround
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MapsListScreen(
    bottomPadding: Dp,
    title: String = stringResource(R.string.mapsList_pickMap),
    vm: MapsListViewModel = koinViewModel(),
    showMultiSelectionActions: Boolean = true,
    multiSelectFAB: @Composable (
        selectedMaps: List<MapFile>, clearSelection: () -> Unit
    ) -> Unit = { _, _ -> },
    onNavigateSettingsRequest: (() -> Unit)? = null,
    onBackClick: (() -> Unit)?,
    onMapPick: (MapFile) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            AnimatedContentShadowWorkaround(vm.selectedMaps.isNotEmpty()) { showMultiSelectFAB ->
                if (showMultiSelectFAB) multiSelectFAB(
                    /*selectedMaps =*/ vm.selectedMaps
                ) { /*clearSelection =*/ vm.selectedMaps.clear() }
            }
        }
    ) {
        MapsListScreen(
            title = title,
            mapsListSegments = vm.availableSegments,
            mapActions = mapActions,
            selectedMaps = vm.selectedMaps,
            listViewOptions = vm.prefs.mapsListOptions,
            showThumbnailsInListPref = vm.prefs.showMapThumbnailsInList,
            showMultiSelectionActions = showMultiSelectionActions,
            extraBottomPadding = bottomPadding,
            settingsButton = onNavigateSettingsRequest?.let { {
                SettingsButton(onClick = it)
            } },
            onToggleMapSelectionRequest = { maps ->
                maps.forEach { map ->
                    val selected = vm.selectedMaps.any { it.path == map.path }
                    vm.selectedMaps.run {
                        if (selected) remove(map)
                        else add(map as MapFile)
                    }
                }
            },
            onClearMapSelectionRequest = {
                vm.selectedMaps.clear()
            },
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
}