package com.aliernfrog.lactool.ui.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R

@Composable
fun SaveWarningDialog(
    onDismissRequest: () -> Unit,
    onKeepEditing: () -> Unit,
    onDiscardChanges: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                onClick = onKeepEditing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(stringResource(R.string.mapsEdit_saveWarning_keepEditing))
            }
        },
        dismissButton = {
            Button(
                onClick = onDiscardChanges,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(stringResource(R.string.mapsEdit_saveWarning_discardChanges))
            }
        },
        icon = {
            Icon(
                painter = rememberVectorPainter(Icons.Rounded.Warning),
                contentDescription = null
            )
        },
        title = {
            Text(stringResource(R.string.mapsEdit_saveWarning_title))
        },
        text = {
            Text(stringResource(R.string.mapsEdit_saveWarning_description))
        }
    )
}