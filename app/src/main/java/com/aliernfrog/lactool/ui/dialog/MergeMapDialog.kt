package com.aliernfrog.lactool.ui.dialog

import androidx.compose.animation.*
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.theme.AppComponentShape

@OptIn(ExperimentalComposeUiApi::class)
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
                    Crossfade(targetState = isMerging) {
                        if (it) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.mapsMerge_merge))
                        }
                    }
                }
            }
        },
        dismissButton = {
            AnimatedVisibility(
                visible = canDismiss,
                exit = fadeOut()
            ) {
                OutlinedButton(
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