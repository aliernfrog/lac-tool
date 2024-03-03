package com.aliernfrog.lactool.ui.screen.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.SettingsConstant
import com.aliernfrog.lactool.data.PrefEditItem
import com.aliernfrog.lactool.externalStorageRoot
import com.aliernfrog.lactool.folderPickerSupportsInitialUri
import com.aliernfrog.lactool.ui.component.form.FormSection
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.util.extension.horizontalFadingEdge
import com.aliernfrog.lactool.util.extension.takePersistablePermissions
import com.aliernfrog.lactool.util.extension.toPath
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.FileUtil
import org.koin.compose.koinInject

@Composable
fun StoragePage(
    onNavigateBackRequest: () -> Unit
) {

    SettingsPageContainer(
        title = stringResource(R.string.settings_storage),
        onNavigateBackRequest = onNavigateBackRequest
    ) {
        FormSection(
            title = stringResource(R.string.settings_storage_folders),
            topDivider = false,
            bottomDivider = false
        ) {
            FolderConfiguration()
        }
    }
}

@Composable
private fun FolderConfiguration(
    prefs: PreferenceManager = koinInject()
) {
    val context = LocalContext.current
    var activePref: PrefEditItem? = remember { null }
    val openFolderLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree(), onResult = {
        if (it == null) return@rememberLauncherForActivityResult
        val pref = activePref ?: return@rememberLauncherForActivityResult
        it.takePersistablePermissions(context)
        pref.setValue(it.toString(), prefs)
    })

    Column {
        SettingsConstant.folders.forEach { pref ->
            FolderCard(
                pref = pref,
                path = getFolderDescription(
                    folder = pref,
                    prefs = prefs
                ),
                onPickFolderRequest = { uri ->
                    activePref = pref
                    openFolderLauncher.launch(uri)
                }
            )
        }
    }
}

@Composable
fun FolderCard(
    pref: PrefEditItem,
    path: String,
    onPickFolderRequest: (Uri?) -> Unit
) {
    val buttonsScrollState = rememberScrollState()
    val recommendedPath = remember { pref.default.removePrefix(externalStorageRoot) }
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
                text = stringResource(pref.labelResourceId),
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
                        onPickFolderRequest(FileUtil.getUriForPath(pref.default))
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
private fun getFolderDescription(
    folder: PrefEditItem,
    prefs: PreferenceManager
): String {
    var text = folder.getValue(prefs)
    if (text.isNotEmpty()) try {
        text = Uri.parse(text).toPath()?.removePrefix(externalStorageRoot)
            ?: text
    } catch (_: Exception) {}
    return text.ifEmpty { stringResource(R.string.settings_storage_folders_notSet) }
}