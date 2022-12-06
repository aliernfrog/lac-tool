package com.aliernfrog.lactool.ui.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R

@Composable
fun DeleteMapDialog(
    mapName: String,
    onDismissRequest: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(stringResource(R.string.action_delete))
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        icon = {
            Icon(
                painter = rememberVectorPainter(Icons.Rounded.DeleteForever),
                contentDescription = null
            )
        },
        text = {
            Text(
                stringResource(R.string.info_deleteQuestion)
                .replace("%NAME%", mapName)
            )
        }
    )
}