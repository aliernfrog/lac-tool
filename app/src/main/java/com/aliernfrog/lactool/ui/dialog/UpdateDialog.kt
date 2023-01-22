package com.aliernfrog.lactool.ui.dialog

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.state.UpdateState

@Composable
fun UpdateDialog(updateState: UpdateState) {
    val uriHandler = LocalUriHandler.current
    if (updateState.updateDialogShown) AlertDialog(
        onDismissRequest = { updateState.updateDialogShown = false },
        confirmButton = {
            Button(
                onClick = { uriHandler.openUri(updateState.newVersionDownload) }
            ) {
                Text(stringResource(R.string.updates_update))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { updateState.updateDialogShown = false }
            ) {
                Text(stringResource(R.string.action_dismiss))
            }
        },
        icon = {
            Icon(
                painter = rememberVectorPainter(Icons.Rounded.Update),
                contentDescription = null
            )
        },
        title = {
            Text(updateState.newVersionName)
        },
        text = {
            Text(
                text = updateState.newVersionBody,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        }
    )
}