package com.aliernfrog.lactool.ui.screen.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.SegmentedButtons
import com.aliernfrog.lactool.ui.component.form.FormSection
import com.aliernfrog.lactool.ui.component.form.SwitchRow
import com.aliernfrog.lactool.ui.theme.Theme
import com.aliernfrog.lactool.ui.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppearancePage(
    settingsViewModel: SettingsViewModel = koinViewModel(),
    onNavigateBackRequest: () -> Unit
) {
    SettingsPageContainer(
        title = stringResource(R.string.settings_appearance),
        onNavigateBackRequest = onNavigateBackRequest
    ) {
        FormSection(
            title = stringResource(R.string.settings_appearance_theme)
        ) {
            SegmentedButtons(
                options = Theme.entries.map { stringResource(it.label) },
                selectedIndex = settingsViewModel.prefs.theme,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                settingsViewModel.prefs.theme = it
            }
        }
        FormSection(
            title = stringResource(R.string.settings_appearance_colors),
            bottomDivider = false
        ) {
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
}