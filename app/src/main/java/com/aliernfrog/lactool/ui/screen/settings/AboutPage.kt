package com.aliernfrog.lactool.ui.screen.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.rememberAsyncImagePainter
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.SettingsConstant
import com.aliernfrog.lactool.ui.component.ButtonIcon
import com.aliernfrog.lactool.ui.component.HorizontalSegmentor
import com.aliernfrog.lactool.ui.component.VerticalSegmentor
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.component.form.FormHeader
import com.aliernfrog.lactool.ui.component.form.FormSection
import com.aliernfrog.lactool.ui.component.form.SwitchRow
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.ui.viewmodel.SettingsViewModel
import com.aliernfrog.lactool.util.extension.resolveString
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPage(
    mainViewModel: MainViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    onNavigateLibsRequest: () -> Unit,
    onNavigateBackRequest: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val appIcon = remember {
        context.packageManager.getApplicationIcon(context.packageName).toBitmap().asImageBitmap()
    }

    SettingsPageContainer(
        title = stringResource(R.string.settings_about),
        onNavigateBackRequest = onNavigateBackRequest
    ) {
        VerticalSegmentor({
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        bitmap = appIcon,
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(72.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = mainViewModel.applicationVersionLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable {
                                settingsViewModel.onAboutClick()
                            }
                        )
                    }
                }
                ChangelogButton(
                    updateAvailable = mainViewModel.updateAvailable
                ) { scope.launch {
                    mainViewModel.updateSheetState.show()
                } }
            }
        }, {
            val socialButtons: List<@Composable () -> Unit> = SettingsConstant.socials.map { social -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable {
                            uriHandler.openUri(social.url)
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        painter = when (val icon = social.icon) {
                            is Int -> painterResource(icon)
                            is ImageVector -> rememberVectorPainter(icon)
                            else -> throw IllegalArgumentException("unexpected class for social icon")
                        },
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(social.label)
                }
            } }

            HorizontalSegmentor(
                *socialButtons.toTypedArray(),
                shape = RectangleShape
            )
        }, modifier = Modifier.padding(horizontal = 16.dp))

        SwitchRow(
            title = stringResource(R.string.settings_about_autoCheckUpdates),
            description = stringResource(R.string.settings_about_autoCheckUpdates_description),
            checked = settingsViewModel.prefs.autoCheckUpdates.value,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            settingsViewModel.prefs.autoCheckUpdates.value = it
        }

        FormSection(
            title = stringResource(R.string.settings_about_credits),
            topDivider = true,
            bottomDivider = true
        ) {
            LaunchedEffect(Unit) {
                SettingsConstant.credits.forEach {
                    it.fetchAvatar()
                }
            }

            SettingsConstant.credits.forEach { data ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(56.dp)
                        .clickable {
                            data.link?.let { uriHandler.openUri(it) }
                        }
                        .padding(
                            vertical = 8.dp,
                            horizontal = 14.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = data.avatarURL?.let {
                            rememberAsyncImagePainter(model = it)
                        } ?: rememberVectorPainter(Icons.Default.Face),
                        contentDescription = null,
                        colorFilter = if (data.avatarURL != null) null else ColorFilter.tint(
                            MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .padding(end = 14.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                    FormHeader(
                        title = data.name.resolveString(),
                        description = data.description.resolveString()
                    )
                }
            }
            ButtonRow(
                title = stringResource(R.string.settings_about_libs),
                description = stringResource(R.string.settings_about_libs_description),
                painter = rememberVectorPainter(Icons.Default.Book),
                arrowRotation = if (LocalLayoutDirection.current == LayoutDirection.Rtl) 270f else 90f,
                expanded = false,
                onClick = onNavigateLibsRequest
            )
        }

        FormSection(
            title = stringResource(R.string.settings_about_other),
            bottomDivider = false
        ) {
            ButtonRow(
                title = stringResource(R.string.settings_about_other_copyDebugInfo),
                description = stringResource(R.string.settings_about_other_copyDebugInfo_description),
                painter = rememberVectorPainter(Icons.Outlined.CopyAll)
            ) {
                clipboardManager.setText(AnnotatedString(mainViewModel.debugInfo))
                settingsViewModel.topToastState.showToast(
                    text = R.string.settings_about_other_copyDebugInfo_copied,
                    icon = Icons.Default.CopyAll
                )
            }
        }
    }
}

@Composable
private fun ChangelogButton(
    updateAvailable: Boolean,
    onClick: () -> Unit
) {
    AnimatedContent(updateAvailable) {
        if (it) ElevatedButton(
            onClick = { onClick() }
        ) {
            ButtonIcon(
                rememberVectorPainter(Icons.Default.Update)
            )
            Text(stringResource(R.string.settings_about_update))
        }
        else OutlinedButton(
            onClick = { onClick() }
        ) {
            ButtonIcon(
                rememberVectorPainter(Icons.Default.Description)
            )
            Text(stringResource(R.string.settings_about_changelog))
        }
    }
}