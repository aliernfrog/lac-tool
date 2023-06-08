package com.aliernfrog.lactool.ui.sheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.AppModalBottomSheet
import com.aliernfrog.lactool.ui.component.ButtonCentered
import com.aliernfrog.lactool.ui.component.TextField
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun AddRoleSheet(
    state: ModalBottomSheetState,
    onRoleAdd: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val roleName = remember { mutableStateOf("") }
    val roleColor = remember { mutableStateOf("") }
    val roleHtml = buildRoleHtml(roleName.value, roleColor.value)
    AppModalBottomSheet(sheetState = state) {
        TextField(
            value = roleName.value,
            onValueChange = { roleName.value = it },
            label = { Text(stringResource(R.string.mapsRoles_roleName)) },
            leadingIcon = rememberVectorPainter(Icons.Rounded.TextFields),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            singleLine = true,
            modifier = Modifier.focusRequester(focusRequester)
        )
        TextField(
            value = roleColor.value,
            onValueChange = { roleColor.value = it },
            label = { Text(stringResource(R.string.mapsRoles_roleColor)) },
            leadingIcon = rememberVectorPainter(Icons.Rounded.Palette),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            singleLine = true
        )
        AnimatedVisibility(
            visible = roleHtml.isNotBlank(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Text(
                text = roleHtml,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Crossfade(targetState = roleName.value.isNotBlank()) {
            ButtonCentered(
                title = stringResource(R.string.mapsRoles_addRole),
                enabled = it,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                onRoleAdd(roleHtml)
                scope.launch { state.hide() }
            }
        }
    }

    LaunchedEffect(state.isVisible) {
        if (state.isVisible) try {
            focusRequester.requestFocus()
            keyboardController?.show()
        } catch (_: Exception) {}
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