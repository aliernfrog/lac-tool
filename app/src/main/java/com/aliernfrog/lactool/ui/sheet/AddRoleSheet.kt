package com.aliernfrog.lactool.ui.sheet

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.laclib.util.LACHelperUtil
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.maps.MapRoleRow
import io.github.aliernfrog.shared.ui.component.AppModalBottomSheet
import io.github.aliernfrog.shared.ui.component.ButtonIcon
import io.github.aliernfrog.shared.ui.component.form.DividerRow
import io.github.aliernfrog.shared.ui.theme.AppComponentShape
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddRoleSheet(
    state: SheetState,
    onRoleAdd: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var roleName by remember { mutableStateOf("") }
    var roleColor by remember { mutableStateOf("") }
    val roleHtml = LACHelperUtil.buildRoleString(name = roleName, color = roleColor) ?: ""

    AppModalBottomSheet(sheetState = state) {
        MapRoleRow(
            role = roleHtml.ifBlank { stringResource(R.string.mapsRoles_addRole_preview) },
            description = if (roleHtml.isBlank()) stringResource(R.string.mapsRoles_addRole_preview_hint) else null,
            alwaysShowRaw = roleHtml.isNotBlank(),
            expanded = null,
            onRoleDelete = {},
            onClick = {},
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .clip(AppComponentShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        )
        DividerRow(Modifier.padding(12.dp))

        OutlinedTextField(
            value = roleName,
            onValueChange = { roleName = it },
            label = {
                Text(stringResource(R.string.mapsRoles_roleName))
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.TextFields,
                    contentDescription = null
                )
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .focusRequester(focusRequester)
        )

        OutlinedTextField(
            value = roleColor,
            onValueChange = { roleColor = it },
            label = {
                Text(stringResource(R.string.mapsRoles_roleColor))
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Palette,
                    contentDescription = null
                )
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 4.dp
                )
        ) {
            Crossfade(roleName.isNotBlank() || roleColor.isNotBlank()) {
                OutlinedButton(
                    enabled = it,
                    shapes = ButtonDefaults.shapes(),
                    onClick = {
                        roleName = ""
                        roleColor = ""
                    }
                ) {
                    ButtonIcon(rememberVectorPainter(Icons.Default.Close))
                    Text(stringResource(R.string.action_clear))
                }
            }
            Crossfade(roleName.isNotBlank()) {
                Button(
                    enabled = it,
                    shapes = ButtonDefaults.shapes(),
                    onClick = {
                        onRoleAdd(roleHtml)
                        scope.launch { state.hide() }
                    }
                ) {
                    ButtonIcon(rememberVectorPainter(Icons.Default.Done))
                    Text(stringResource(R.string.mapsRoles_addRole))
                }
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