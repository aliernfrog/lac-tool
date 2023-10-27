package com.aliernfrog.lactool.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.theme.AppComponentShape

@Composable
fun MergeMapDialog(
    isMerging: Boolean,
    onDismissRequest: () -> Unit,
    onConfirm: (mapName: String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var mapName by remember { mutableStateOf("") }
    val canDismiss = !isMerging
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Crossfade(targetState = mapName.isNotBlank() && !isMerging) { enabled ->
                Button(
                    onClick = { if (!isMerging) onConfirm(mapName) },
                    enabled = enabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.animateContentSize()
                ) {
                    // Make the text invisible instead of removing, so it keeps the button width
                    // stole from https://github.com/vendetta-mod/VendettaManager
                    Box {
                        Text(
                            text = stringResource(R.string.mapsMerge_merge),
                            color = if (isMerging) Color.Transparent else LocalContentColor.current
                        )
                        if (isMerging) CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.Center),
                        )
                    }
                }
            }
        },
        dismissButton = {
            AnimatedVisibility(
                visible = canDismiss,
                exit = fadeOut()
            ) {
                TextButton(
                    onClick = { if (canDismiss) onDismissRequest() }
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        },
        title = {
            Crossfade(targetState = isMerging) {
                Text(stringResource(if (it) R.string.mapsMerge_merging else R.string.mapsMerge_pickName))
            }
        },
        text = {
            OutlinedTextField(
                value = mapName,
                onValueChange = { mapName = it },
                shape = AppComponentShape,
                singleLine = true,
                enabled = canDismiss,
                modifier = Modifier.focusRequester(focusRequester)
            )
        },
        properties = DialogProperties(
            dismissOnBackPress = canDismiss,
            dismissOnClickOutside = canDismiss
        )
    )
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
            keyboardController?.show()
        } catch (_: java.lang.Exception) {}
    }
}
