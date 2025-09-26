package com.aliernfrog.lactool.ui.screen.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.SettingsConstant
import com.aliernfrog.lactool.data.PrefEditItem
import com.aliernfrog.lactool.enum.StorageAccessType
import com.aliernfrog.lactool.enum.isCompatible
import com.aliernfrog.lactool.externalStorageRoot
import com.aliernfrog.lactool.folderPickerSupportsInitialUri
import com.aliernfrog.lactool.ui.component.ButtonIcon
import com.aliernfrog.lactool.ui.component.FadeVisibility
import com.aliernfrog.lactool.ui.component.VerticalSegmentor
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveRowHeader
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveSection
import com.aliernfrog.lactool.ui.component.form.ExpandableRow
import com.aliernfrog.lactool.ui.viewmodel.SettingsViewModel
import com.aliernfrog.lactool.util.extension.horizontalFadingEdge
import com.aliernfrog.lactool.util.extension.resolveString
import com.aliernfrog.lactool.util.extension.takePersistablePermissions
import com.aliernfrog.lactool.util.extension.toPath
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.manager.base.BasePreferenceManager
import com.aliernfrog.lactool.util.staticutil.FileUtil
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun StoragePage(
    settingsViewModel: SettingsViewModel = koinViewModel(),
    onNavigateBackRequest: () -> Unit
) {
    var storageAccessTypesExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    val selectedStorageAccessType = StorageAccessType.entries[settingsViewModel.prefs.storageAccessType.value]

    SettingsPageContainer(
        title = stringResource(R.string.settings_storage),
        onNavigateBackRequest = onNavigateBackRequest
    ) {
        VerticalSegmentor(
            {
                ExpandableRow(
                    expanded = storageAccessTypesExpanded,
                    title = stringResource(R.string.settings_storage_storageAccessType),
                    description = stringResource(selectedStorageAccessType.label),
                    onClickHeader = { storageAccessTypesExpanded = !storageAccessTypesExpanded }
                ) {
                    val choices: List<@Composable () -> Unit> = StorageAccessType.entries.filter { it.isCompatible() }.map { type -> {
                        val selected = settingsViewModel.prefs.storageAccessType.value == type.ordinal
                        fun onSelect() {
                            if (!selected) type.enable(settingsViewModel.prefs)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = ::onSelect)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = ::onSelect
                            )
                            ExpressiveRowHeader(
                                title = stringResource(type.label),
                                description = stringResource(type.description)
                            )
                        }
                    } }
                    VerticalSegmentor(
                        *choices.toTypedArray(),
                        modifier = Modifier.padding(
                            start = 12.dp,
                            end = 12.dp,
                            top = 4.dp,
                            bottom = 8.dp
                        )
                    )
                }
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        ExpressiveSection(stringResource(R.string.settings_storage_folders)) {
            FolderConfiguration(
                useRawPathInputs = selectedStorageAccessType != StorageAccessType.SAF
            )
        }
    }
}

@Composable
private fun FolderConfiguration(
    prefs: PreferenceManager = koinInject(),
    useRawPathInputs: Boolean
) {
    val context = LocalContext.current
    var activePref: PrefEditItem<String>? = remember { null }
    val openFolderLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree(), onResult = { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val pref = activePref ?: return@rememberLauncherForActivityResult
        if (!useRawPathInputs) uri.takePersistablePermissions(context)
        pref.preference(prefs).value = uri.toString().let {
            if (useRawPathInputs) FileUtil.getFilePath(it) else it
        }
    })

    AnimatedContent(useRawPathInputs) { rawPathInput ->
        val items: List<@Composable () -> Unit> = SettingsConstant.folders.map { prefEditItem -> {
            val pref = prefEditItem.preference(prefs)
            val label = prefEditItem.label(prefs).resolveString()

            if (rawPathInput) RawPathItem(
                label = label,
                pref = pref,
                onPickFolderRequest = {
                    activePref = prefEditItem
                    openFolderLauncher.launch(null)
                }
            )
            else FolderConfigItem(
                label = label,
                pref = pref,
                path = getFolderDescription(pref.value),
                onPickFolderRequest = { uri ->
                    activePref = prefEditItem
                    openFolderLauncher.launch(uri)
                }
            )
        } }

        VerticalSegmentor(
            *items.toTypedArray(),
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RawPathItem(
    label: String,
    pref: BasePreferenceManager.Preference<String>,
    onPickFolderRequest: () -> Unit
) {
    val currentPath = pref.value
    val isDefault = pref.defaultValue == currentPath
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = currentPath,
            onValueChange = {
                pref.value = it
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        FadeVisibility(!isDefault) {
            SuggestionCard(
                title = stringResource(R.string.settings_storage_folders_restoreDefault),
                description = pref.defaultValue
            ) {
                pref.value = pref.defaultValue
            }
        }

        FilledTonalButton(
            onClick = onPickFolderRequest,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier.align(Alignment.End)
        ) {
            ButtonIcon(rememberVectorPainter(Icons.Default.FolderOpen))
            Text(stringResource(R.string.settings_storage_folders_choose))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FolderConfigItem(
    label: String,
    pref: BasePreferenceManager.Preference<String>,
    path: String,
    onPickFolderRequest: (Uri?) -> Unit
) {
    val buttonsScrollState = rememberScrollState()
    val recommendedPath = remember { pref.defaultValue.removePrefix(externalStorageRoot) }
    val dataFolderPath = remember { externalStorageRoot+"Android/data" }
    val usingRecommendedPath = path.equals(recommendedPath, ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            text = path,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 15.sp
        )

        if (!usingRecommendedPath) SuggestionCard(
            title = stringResource(
                if (folderPickerSupportsInitialUri) R.string.settings_storage_folders_openRecommended
                else R.string.settings_storage_folders_recommendedFolder
            ),
            description = recommendedPath,
            enabled = folderPickerSupportsInitialUri
        ) {
            onPickFolderRequest(FileUtil.getUriForPath(pref.defaultValue))
        }

        Row(
            modifier = Modifier
                .horizontalFadingEdge(
                    scrollState = buttonsScrollState,
                    edgeColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    isRTL = LocalLayoutDirection.current == LayoutDirection.Rtl
                )
                .horizontalScroll(buttonsScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = { onPickFolderRequest(null) },
                label = {
                    Text(stringResource(R.string.settings_storage_folders_choose))
                }
            )

            AssistChip(
                onClick = {
                    onPickFolderRequest(FileUtil.getUriForPath(path))
                },
                label = {
                    Text(stringResource(R.string.settings_storage_folders_openCurrent))
                }
            )

            if (folderPickerSupportsInitialUri) AssistChip(
                onClick = {
                    onPickFolderRequest(FileUtil.getUriForPath(dataFolderPath))
                },
                label = {
                    Text(stringResource(R.string.settings_storage_folders_openAndroidData))
                }
            )
        }
    }
}

@Composable
private fun SuggestionCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedCard(
        enabled = enabled,
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(
            vertical = 4.dp,
            horizontal = 16.dp
        )) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun getFolderDescription(path: String): String {
    var text = path
    if (text.isNotEmpty()) try {
        text = text.toUri().toPath().removePrefix(externalStorageRoot)
    } catch (_: Exception) {}
    return text.ifEmpty { stringResource(R.string.settings_storage_folders_notSet) }
}