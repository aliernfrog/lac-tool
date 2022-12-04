package com.aliernfrog.lactool.ui.sheet

import androidx.compose.animation.Crossfade
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.composable.LACToolButtonCentered
import com.aliernfrog.lactool.ui.composable.LACToolModalBottomSheet
import com.aliernfrog.lactool.ui.composable.LACToolTextField
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddRoleSheet(state: ModalBottomSheetState, onRoleAdd: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val roleName = remember { mutableStateOf("") }
    LACToolModalBottomSheet(sheetState = state) {
        LACToolTextField(
            value = roleName.value,
            onValueChange = { roleName.value = it },
            label = { Text(stringResource(R.string.mapsRoles_roleName)) },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Crossfade(targetState = roleName.value.isNotBlank()) {
            LACToolButtonCentered(
                title = stringResource(R.string.mapsRoles_addRole),
                enabled = it,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                onRoleAdd(roleName.value)
                scope.launch { state.hide() }
            }
        }
    }
}