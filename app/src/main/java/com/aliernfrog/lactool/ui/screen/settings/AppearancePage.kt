package com.aliernfrog.lactool.ui.screen.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.RadioButtons
import com.aliernfrog.lactool.ui.component.form.ExpandableRow
import com.aliernfrog.lactool.ui.component.form.SwitchRow
import com.aliernfrog.lactool.ui.theme.Theme
import com.aliernfrog.lactool.ui.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppearancePage(
    settingsViewModel: SettingsViewModel = koinViewModel(),
    onNavigateBackRequest: () -> Unit
) {
    var themeOptionsExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    SettingsPageContainer(
        title = stringResource(R.string.settings_appearance),
        onNavigateBackRequest = onNavigateBackRequest
    ) {
        ExpandableRow(
            expanded = themeOptionsExpanded,
            title = stringResource(R.string.settings_appearance_theme),
            description = stringResource(R.string.settings_appearance_theme_description),
            painter = rememberVectorPainter(Icons.Outlined.DarkMode),
            trailingButtonText = stringResource(Theme.entries[settingsViewModel.prefs.theme].label),
            onClickHeader = {
                themeOptionsExpanded = !themeOptionsExpanded
            }
        ) {
            RadioButtons(
                options = Theme.entries.map { stringResource(it.label) },
                selectedOptionIndex = settingsViewModel.prefs.theme
            ) {
                settingsViewModel.prefs.theme = it
            }
        }
        if (settingsViewModel.showMaterialYouOption) SwitchRow(
            title = stringResource(R.string.settings_appearance_materialYou),
            description = stringResource(R.string.settings_appearance_materialYou_description),
            painter = rememberVectorPainter(Icons.Outlined.Brush),
            checked = settingsViewModel.prefs.materialYou
        ) {
            settingsViewModel.prefs.materialYou = it
        }
        SwitchRow(
            title = stringResource(R.string.settings_appearance_pitchBlack),
            description = stringResource(R.string.settings_appearance_pitchBlack_description),
            painter = rememberVectorPainter(Icons.Outlined.Contrast),
            checked = settingsViewModel.prefs.pitchBlack
        ) {
            settingsViewModel.prefs.pitchBlack = it
        }
    }
}