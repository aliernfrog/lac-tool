package com.aliernfrog.lactool.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R

@Composable
fun AlphaWarningDialog(
    onDismissRequest: (acknowledged: Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismissRequest(false) },
        text = {
            Text(stringResource(R.string.warning_alphaVersion))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest(true)
                }
            ) {
                Text(stringResource(R.string.action_ok))
            }
        }
    )
}