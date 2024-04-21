package com.aliernfrog.lactool.ui.screen.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.SettingsConstant
import com.aliernfrog.lactool.ui.component.ButtonIcon
import com.aliernfrog.lactool.ui.component.form.FormSection
import com.aliernfrog.lactool.ui.component.form.SwitchRow
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.ui.viewmodel.SettingsViewModel
import com.aliernfrog.lactool.util.extension.horizontalFadingEdge
import com.aliernfrog.lactool.util.extension.resolveString
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPage(
    mainViewModel: MainViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    onNavigateBackRequest: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val appIcon = remember {
        context.packageManager.getApplicationIcon(context.packageName)
            .toBitmap().asImageBitmap()
    }
    val appVersion = remember {
        "${mainViewModel.applicationVersionName} (${mainViewModel.applicationVersionCode})"
    }

    SettingsPageContainer(
        title = stringResource(R.string.settings_about),
        onNavigateBackRequest = onNavigateBackRequest
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.aligned(Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Image(
                    bitmap = appIcon,
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(72.dp)
                )
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = appVersion,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable {
                            settingsViewModel.onAboutClick()
                        }
                    )
                }
            }
            UpdateButton(
                updateAvailable = mainViewModel.updateAvailable
            ) { updateAvailable -> scope.launch {
                if (updateAvailable) mainViewModel.updateSheetState.show()
                else mainViewModel.checkUpdates(manuallyTriggered = true)
            } }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsConstant.socials.forEach { social ->
                    TextButton(
                        onClick = { uriHandler.openUri(social.url) }
                    ) {
                        ButtonIcon(when (val icon = social.icon) {
                            is Int -> painterResource(icon)
                            is ImageVector -> rememberVectorPainter(icon)
                            else -> throw IllegalArgumentException("unexpected class for social icon")
                        })
                        Text(social.label)
                    }
                }
            }

            val creditsScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .horizontalFadingEdge(
                        scrollState = creditsScrollState,
                        edgeColor = MaterialTheme.colorScheme.surface,
                        isRTL = LocalLayoutDirection.current == LayoutDirection.Rtl
                    )
                    .horizontalScroll(creditsScrollState)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsConstant.credits.forEach { data ->
                    ElevatedCard(
                        onClick = { uriHandler.openUri(data.url) }
                    ) {
                        Text(
                            text = data.name.resolveString(),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                        Text(
                            text = data.description.resolveString(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        SwitchRow(
            title = stringResource(R.string.settings_about_autoCheckUpdates),
            description = stringResource(R.string.settings_about_autoCheckUpdates_description),
            checked = settingsViewModel.prefs.autoCheckUpdates
        ) {
            settingsViewModel.prefs.autoCheckUpdates = it
        }

        FormSection(
            title = stringResource(R.string.settings_about_changelog),
            topDivider = true,
            bottomDivider = false
        ) {
            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                shape = AppComponentShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                MarkdownText(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    markdown = mainViewModel.latestVersionInfo.body,
                    style = LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    linkColor = MaterialTheme.colorScheme.primary,
                    onLinkClicked = { uriHandler.openUri(it) }
                )
            }
        }
    }
}

@Composable
private fun UpdateButton(
    updateAvailable: Boolean,
    onClick: (updateAvailable: Boolean) -> Unit
) {
    AnimatedContent(updateAvailable) {
        if (it) ElevatedButton(
            onClick = { onClick(true) }
        ) {
            ButtonIcon(
                rememberVectorPainter(Icons.Default.Update)
            )
            Text(stringResource(R.string.settings_about_update))
        }
        else OutlinedButton(
            onClick = { onClick(false) }
        ) {
            ButtonIcon(
                rememberVectorPainter(Icons.Default.Refresh)
            )
            Text(stringResource(R.string.settings_about_checkUpdates))
        }
    }
}