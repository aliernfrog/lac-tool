package com.aliernfrog.lactool.ui.screen.maps

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.impl.mapActions
import com.aliernfrog.lactool.ui.component.SettingsButton
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel
import io.github.aliernfrog.pftool_shared.ui.screen.maps.MapDetailsScreen

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapDetailsScreen(
    map: MapFile,
    vm: MapsViewModel,
    onNavigateSettingsRequest: () -> Unit,
    onNavigateBackRequest: (() -> Unit)?
) {
    MapDetailsScreen(
        map = map,
        mapActions = mapActions,
        showMapThumbnail = vm.prefs.showChosenMapThumbnail.value,
        showMapNameFieldGuide = vm.prefs.showMapNameFieldGuide.value,
        settingsButton = {
            SettingsButton { onNavigateSettingsRequest() }
        },
        onDismissMapNameFieldGuide = { vm.prefs.showMapNameFieldGuide.value = false },
        onViewThumbnailRequest = {
            vm.openMapThumbnailViewer(map)
        },
        onNavigateBackRequest = onNavigateBackRequest
    )
}