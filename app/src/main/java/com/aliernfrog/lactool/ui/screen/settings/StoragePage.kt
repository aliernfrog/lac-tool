package com.aliernfrog.lactool.ui.screen.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
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
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.SettingsConstant
import com.aliernfrog.lactool.data.PrefEditItem
import com.aliernfrog.lactool.enum.StorageAccessType
import com.aliernfrog.lactool.enum.isCompatible
import com.aliernfrog.lactool.externalStorageRoot
import com.aliernfrog.lactool.folderPickerSupportsInitialUri
import com.aliernfrog.lactool.ui.component.FadeVisibility
import com.aliernfrog.lactool.ui.component.form.DividerRow
import com.aliernfrog.lactool.ui.component.form.ExpandableRow
import com.aliernfrog.lactool.ui.component.form.FormHeader
import com.aliernfrog.lactool.ui.component.form.FormSection
import com.aliernfrog.lactool.ui.theme.AppComponentShape
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
        ExpandableRow(
            expanded = storageAccessTypesExpanded,
            title = stringResource(R.string.settings_storage_storageAccessType),
            painter = rememberVectorPainter(Icons.Outlined.FolderOpen),
            trailingButtonText = stringResource(selectedStorageAccessType.label),
            onClickHeader = { storageAccessTypesExpanded = !storageAccessTypesExpanded }
        ) {
            StorageAccessType.entries.filter { it.isCompatible() }.forEach { type ->
                val selected = settingsViewModel.prefs.storageAccessType.value == type.ordinal
                fun onSelect() {
                    if (!selected) type.enable(settingsViewModel.prefs)
                }
                if (type.ordinal != 0) DividerRow()
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
                    FormHeader(
                        title = stringResource(type.label),
                        description = stringResource(type.description)
                    )
                }
            }
        }
        FormSection(
            title = stringResource(R.string.settings_storage_folders),
            topDivider = true,
            bottomDivider = false
        ) {
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
        Column {
            SettingsConstant.folders.forEach { prefEditItem ->
                val pref = prefEditItem.preference(prefs)
                val label = prefEditItem.label(prefs).resolveString()
                if (rawPathInput) RawPathInput(
                    label = label,
                    pref = pref,
                    onPickFolderRequest = {
                        activePref = prefEditItem
                        openFolderLauncher.launch(null)
                    }
                )
                else FolderCard(
                    label = label,
                    pref = pref,
                    path = getFolderDescription(pref.value),
                    onPickFolderRequest = { uri ->
                        activePref = prefEditItem
                        openFolderLauncher.launch(uri)
                    }
                )
            }
        }
    }
}

@Composable
fun RawPathInput(
    label: String,
    pref: BasePreferenceManager.Preference<String>,
    onPickFolderRequest: () -> Unit
) {
    val currentPath = pref.value
    val isDefault = pref.defaultValue == currentPath
    OutlinedTextField(
        value = currentPath,
        onValueChange = {
            pref.value = it
        },
        label = {
            Text(label)
        },
        supportingText = {
            FadeVisibility(!isDefault) {
                Text(
                    stringResource(R.string.settings_storage_folders_default).replace("%s", pref.defaultValue)
                )
            }
        },
        trailingIcon = {
            Row {
                Crossfade(!isDefault) { enabled ->
                    IconButton(
                        onClick = { pref.value = pref.defaultValue },
                        enabled = enabled
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = stringResource(R.string.settings_storage_folders_restoreDefault)
                        )
                    }
                }
                IconButton(
                    onClick = { onPickFolderRequest() }
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = stringResource(R.string.settings_storage_folders_choose)
                    )
                }
            }
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}

@Composable
fun FolderCard(
    label: String,
    pref: BasePreferenceManager.Preference<String>,
    path: String,
    onPickFolderRequest: (Uri?) -> Unit
) {
    val buttonsScrollState = rememberScrollState()
    val recommendedPath = remember { pref.defaultValue.removePrefix(externalStorageRoot) }
    val dataFolderPath = remember { externalStorageRoot+"Android/data" }
    val usingRecommendedPath = path.equals(recommendedPath, ignoreCase = true)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = AppComponentShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
            Text(path)

            if (!usingRecommendedPath) {
                Text(
                    text = stringResource(R.string.settings_storage_folders_recommendedFolder),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(recommendedPath)
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
                if (folderPickerSupportsInitialUri) AssistChip(
                    onClick = {
                        onPickFolderRequest(FileUtil.getUriForPath(pref.defaultValue))
                    },
                    label = {
                        Text(stringResource(R.string.settings_storage_folders_openRecommended))
                    }
                )

                AssistChip(
                    onClick = { onPickFolderRequest(null) },
                    label = {
                        Text(stringResource(R.string.settings_storage_folders_choose))
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
}

@Composable
private fun getFolderDescription(path: String): String {
    var text = path
    if (text.isNotEmpty()) try {
        text = Uri.parse(text).toPath().removePrefix(externalStorageRoot)
    } catch (_: Exception) {}
    return text.ifEmpty { stringResource(R.string.settings_storage_folders_notSet) }
}
