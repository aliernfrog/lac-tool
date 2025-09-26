package com.aliernfrog.lactool.ui.screen.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.VerticalSegmentor
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveSection
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveSwitchRow
import com.aliernfrog.lactool.ui.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MapsPage(
    settingsViewModel: SettingsViewModel = koinViewModel(),
    onNavigateBackRequest: () -> Unit
) {
    SettingsPageContainer(
        title = stringResource(R.string.settings_maps),
        onNavigateBackRequest = onNavigateBackRequest
    ) {
        ExpressiveSection(stringResource(R.string.settings_maps_thumbnails)) {
            VerticalSegmentor(
                {
                    ExpressiveSwitchRow(
                        title = stringResource(R.string.settings_maps_thumbnails_chosen),
                        description = stringResource(R.string.settings_maps_thumbnails_chosen_description),
                        checked = settingsViewModel.prefs.showChosenMapThumbnail.value
                    ) {
                        settingsViewModel.prefs.showChosenMapThumbnail.value = it
                    }
                },
                {
                    ExpressiveSwitchRow(
                        title = stringResource(R.string.settings_maps_thumbnails_list),
                        description = stringResource(R.string.settings_maps_thumbnails_list_description),
                        checked = settingsViewModel.prefs.showMapThumbnailsInList.value
                    ) {
                        settingsViewModel.prefs.showMapThumbnailsInList.value = it
                    }
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}