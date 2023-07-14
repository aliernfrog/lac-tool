package com.aliernfrog.lactool.ui.dialog

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.SettingsBackupRestore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.SettingsConstant
import com.aliernfrog.lactool.data.PrefEditItem
import com.aliernfrog.lactool.ui.component.ScrollableRow
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.util.extension.applyPathOptionPreset
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import com.aliernfrog.lactool.util.staticutil.UriToFileUtil
import com.aliernfrog.toptoast.enum.TopToastType
import com.aliernfrog.toptoast.state.TopToastState

@Composable
fun PathOptionsDialog(
    topToastState: TopToastState,
    prefs: PreferenceManager,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val pathOptions = SettingsConstant.pathOptions
    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            Button(
                onClick = {
                    pathOptions.forEach { option ->
                        prefs.putString(option.key, option.mutableValue.value)
                    }
                    topToastState.showToast(
                        text = R.string.settings_general_pathOptions_saved,
                        icon = Icons.Rounded.Done,
                        type = TopToastType.ANDROID
                    )
                    GeneralUtil.restartApp(context)
                }
            ) {
                Text(stringResource(R.string.action_done))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { onDismissRequest() }) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        icon = {
            Icon(
                painter = rememberVectorPainter(Icons.Rounded.Folder),
                contentDescription = null
            )
        },
        title = {
            Text(stringResource(R.string.settings_general_pathOptions))
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.settings_general_pathOptions_willRestart), fontSize = 14.sp, lineHeight = 18.sp)
                ScrollableRow(gradientColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)) {
                    SettingsConstant.pathOptionPresets.forEach { preset ->
                        SuggestionChip(
                            onClick = {
                                pathOptions.forEach { option ->
                                    option.applyPathOptionPreset(preset)
                                }
                            },
                            label = { Text(stringResource(preset.labelResourceId)) },
                            shape = AppComponentShape,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                    }
                }
                pathOptions.forEach { option ->
                    PathOption(option)
                    LaunchedEffect(Unit) {
                        option.mutableValue.value = prefs.getString(option.key, option.default)
                    }
                }
            }
        }
    )
}

@Composable
private fun PathOption(option: PrefEditItem) {
    val treePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = {
            if (it != null) option.mutableValue.value = UriToFileUtil.getRealFolderPath(it)
        }
    )
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = option.mutableValue.value,
        onValueChange = { option.mutableValue.value = it },
        singleLine = true,
        shape = AppComponentShape,
        label = {
            Text(stringResource(option.labelResourceId!!))
        },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(visible = option.mutableValue.value != option.default) {
                    IconButton(
                        onClick = { option.mutableValue.value = option.default }
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Rounded.SettingsBackupRestore),
                            contentDescription = stringResource(R.string.settings_general_pathOptions_restoreDefault)
                        )
                    }
                }
                IconButton(
                    onClick = { treePicker.launch(null) }
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Rounded.Folder),
                        contentDescription = stringResource(R.string.settings_general_pathOptions_pickDirectory)
                    )
                }
            }
        }
    )
}