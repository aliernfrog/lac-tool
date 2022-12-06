package com.aliernfrog.lactool.ui.sheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.composable.LACToolButtonCentered
import com.aliernfrog.lactool.ui.composable.LACToolModalBottomSheet
import com.aliernfrog.lactool.ui.composable.LACToolTextField
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun AddRoleSheet(state: ModalBottomSheetState, onRoleAdd: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val roleName = remember { mutableStateOf("") }
    val roleColor = remember { mutableStateOf("") }
    val roleHtml = buildRoleHtml(roleName.value, roleColor.value)
    LACToolModalBottomSheet(sheetState = state) {
        LACToolTextField(
            value = roleName.value,
            onValueChange = { roleName.value = it },
            label = { Text(stringResource(R.string.mapsRoles_roleName)) },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.focusRequester(focusRequester)
        )
        LACToolTextField(
            value = roleColor.value,
            onValueChange = { roleColor.value = it },
            label = { Text(stringResource(R.string.mapsRoles_roleColor)) },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
        AnimatedVisibility(visible = roleHtml.isNotBlank()) {
            Text(
                text = roleHtml,
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Crossfade(targetState = roleName.value.isNotBlank()) {
            LACToolButtonCentered(
                title = stringResource(R.string.mapsRoles_addRole),
                enabled = it,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                onRoleAdd(roleHtml)
                scope.launch { state.hide() }
            }
        }
    }

    LaunchedEffect(state.targetValue) {
        if (state.targetValue == ModalBottomSheetValue.Expanded) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }
}

private fun buildRoleHtml(name: String, color: String): String {
    if (name.isBlank()) return ""
    var html = name
    if (!html.contains("[")) html = "[$html"
    if (!html.contains("]")) html = "$html]"
    if (color.isNotBlank()) html = "<color=$color>$html</color>"
    return html
}