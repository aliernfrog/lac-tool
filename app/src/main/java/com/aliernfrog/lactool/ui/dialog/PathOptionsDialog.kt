package com.aliernfrog.lactool.ui.dialog

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.SettingsBackupRestore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.AppComponentShape
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.SettingsConstant
import com.aliernfrog.lactool.data.PrefEditItem

@Composable
fun PathOptionsDialog(config: SharedPreferences, onDismissRequest: (saved: Boolean) -> Unit) {
    val pathOptions = SettingsConstant.pathOptions
    AlertDialog(
        onDismissRequest = { onDismissRequest(false) },
        confirmButton = {
            Button(
                onClick = {
                    val configEditor = config.edit()
                    pathOptions.forEach { option ->
                        configEditor.putString(option.key, option.mutableValue.value)
                    }
                    configEditor.apply()
                    onDismissRequest(true)
                }
            ) {
                Text(stringResource(R.string.action_done))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { onDismissRequest(false) }) {
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
                pathOptions.forEach { option ->
                    PathOption(option, config)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PathOption(option: PrefEditItem, config: SharedPreferences) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = option.mutableValue.value,
        onValueChange = { option.mutableValue.value = it },
        shape = AppComponentShape,
        label = {
            Text(stringResource(option.labelResourceId!!))
        },
        placeholder = {
            Text(option.default)
        },
        trailingIcon = {
            IconButton(
                onClick = { option.mutableValue.value = option.default }
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Rounded.SettingsBackupRestore),
                    contentDescription = stringResource(R.string.settings_general_pathOptions_restoreDefault)
                )
            }
        },
        singleLine = true
    )

    LaunchedEffect(Unit) {
        option.mutableValue.value = config.getString(option.key, option.default) ?: option.default
    }
}