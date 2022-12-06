package com.aliernfrog.lactool.ui.sheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.composable.LACToolButtonCentered
import com.aliernfrog.lactool.ui.composable.LACToolModalBottomSheet
import com.aliernfrog.lactool.ui.composable.LACToolTextField
import com.aliernfrog.lactool.ui.dialog.ColorPickerDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun AddRoleSheet(state: ModalBottomSheetState, onRoleAdd: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val colorPickerVisible = remember { mutableStateOf(false) }
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            LACToolTextField(
                value = roleColor.value,
                onValueChange = { roleColor.value = it },
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.mapsRoles_roleColor)) },
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
            IconButton(
                onClick = { colorPickerVisible.value = true },
                modifier = Modifier.padding(end = 8.dp).size(40.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Outlined.Palette),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
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

    if (colorPickerVisible.value) ColorPickerDialog(
        onDismissRequest = { colorPickerVisible.value = false },
        onColorPick = {
            roleColor.value = "#${it.value.toString(16).slice(2..7)}"
            colorPickerVisible.value = false
        },
        initialColor = Color(android.graphics.Color.parseColor(if (roleColor.value.startsWith("#")) roleColor.value else "#ffffff"))
    )

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