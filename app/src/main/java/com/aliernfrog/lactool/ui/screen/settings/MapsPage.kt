package com.aliernfrog.lactool.ui.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.form.FormSection
import com.aliernfrog.lactool.ui.component.form.SwitchRow
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
        FormSection(
            title = stringResource(R.string.settings_maps_thumbnails),
            bottomDivider = false
        ) {
            SwitchRow(
                title = stringResource(R.string.settings_maps_thumbnails_chosen),
                description = stringResource(R.string.settings_maps_thumbnails_chosen_description),
                checked = settingsViewModel.prefs.showChosenMapThumbnail.value
            ) {
                settingsViewModel.prefs.showChosenMapThumbnail.value = it
            }
            SwitchRow(
                title = stringResource(R.string.settings_maps_thumbnails_list),
                description = stringResource(R.string.settings_maps_thumbnails_list_description),
                checked = settingsViewModel.prefs.showMapThumbnailsInList.value
            ) {
                settingsViewModel.prefs.showMapThumbnailsInList.value = it
            }
        }
    }
}