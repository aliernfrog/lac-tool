package com.aliernfrog.lactool.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LacMapType
import com.aliernfrog.lactool.enum.LACMapOptionType
import com.aliernfrog.lactool.state.MapsEditState
import com.aliernfrog.lactool.ui.composable.*
import com.aliernfrog.lactool.util.Destination
import kotlinx.coroutines.launch

@Composable
fun MapsEditScreen(mapsEditState: MapsEditState, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize()) {
        Actions(mapsEditState, navController)
        LACToolFAB(
            icon = Icons.Default.Done,
            modifier = Modifier.align(Alignment.BottomEnd),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            scope.launch { mapsEditState.finishEditing(navController, true, context) }
        }
    }
}

@Composable
private fun Actions(mapsEditState: MapsEditState, navController: NavController) {
    Column(Modifier.animateContentSize().verticalScroll(mapsEditState.scrollState)) {
        GeneralActions(mapsEditState, navController)
        OptionsActions(mapsEditState)
        Spacer(Modifier.height(70.dp))
    }
}

@Composable
private fun GeneralActions(mapsEditState: MapsEditState, navController: NavController) {
    val typesExpanded = remember { mutableStateOf(false) }
    LACToolColumnDivider(title = stringResource(R.string.mapsEdit_general)) {
        AnimatedVisibilityColumn(visible = mapsEditState.mapData.value?.serverName != null) {
            TextField(
                label = stringResource(R.string.mapsEdit_serverName),
                value = mapsEditState.mapData.value?.serverName?.value ?: "",
                onValueChange = { mapsEditState.mapData.value?.serverName?.value = it }
            )
        }
        AnimatedVisibilityColumn(visible = mapsEditState.mapData.value?.mapType != null) {
            LACToolButtonShapeless(
                title = stringResource(R.string.mapsEdit_mapType),
                description = getMapTypes().find { it.index == mapsEditState.mapData.value?.mapType?.value }?.label ?: "unknown",
                expanded = typesExpanded.value
            ) {
                typesExpanded.value = !typesExpanded.value
            }
            AnimatedVisibilityColumn(visible = typesExpanded.value) {
                LACToolColumnRounded(Modifier.padding(horizontal = 8.dp)) {
                    LACToolRadioButtons(
                        options = getMapTypes().map { it.label },
                        initialIndex = mapsEditState.mapData.value?.mapType?.value ?: 0,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        optionsRounded = true,
                        onSelect = { mapsEditState.mapData.value?.mapType?.value = it }
                    )
                }
            }
        }
        AnimatedVisibilityColumn(visible = mapsEditState.mapData.value?.mapRoles != null) {
            LACToolButtonShapeless(
                title = stringResource(R.string.mapsRoles),
                description = stringResource(R.string.mapsRolesDescription).replace("%COUNT%", mapsEditState.mapData.value?.mapRoles?.size.toString()),
                expanded = false,
                arrowRotation = 90f
            ) {
                navController.navigate(Destination.MAPS_ROLES.route)
            }
        }
    }
}

@Composable
private fun OptionsActions(mapsEditState: MapsEditState) {
    AnimatedVisibilityColumn(visible = !mapsEditState.mapData.value?.mapOptions.isNullOrEmpty()) {
        LACToolColumnDivider(title = stringResource(R.string.mapsEdit_options), bottomDivider = false) {
            mapsEditState.mapData.value?.mapOptions?.forEach { option ->
                when (option.type) {
                    LACMapOptionType.NUMBER -> TextField(
                        label = option.label,
                        value = option.value.value,
                        onValueChange = { option.value.value = it },
                        placeholder = option.value.value,
                        numberOnly = true
                    )
                    LACMapOptionType.BOOLEAN -> LACToolSwitch(
                        title = option.label,
                        checked = option.value.value == "true",
                        onCheckedChange = { option.value.value = it.toString() }
                    )
                    LACMapOptionType.SWITCH -> LACToolSwitch(
                        title = option.label,
                        checked = option.value.value == "enabled",
                        onCheckedChange = { option.value.value = if (it) "enabled" else "disabled" }
                    )
                }
            }
        }
    }
}

@Composable
private fun TextField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String? = null, numberOnly: Boolean = false) {
    LACToolTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { if (placeholder != null) { Text(placeholder) } },
        keyboardOptions = if (numberOnly) KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        singleLine = true,
        containerColor = MaterialTheme.colorScheme.surface,
        rounded = false
    )
}

@Composable
private fun AnimatedVisibilityColumn(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(visible, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
        Column { content() }
    }
}

@Composable
private fun getMapTypes(): List<LacMapType> {
    val context = LocalContext.current
    return listOf(
        LacMapType(0, context.getString(R.string.mapsEdit_mapType_0)),
        LacMapType(1, context.getString(R.string.mapsEdit_mapType_1)),
        LacMapType(2, context.getString(R.string.mapsEdit_mapType_2)),
        LacMapType(3, context.getString(R.string.mapsEdit_mapType_3)),
        LacMapType(4, context.getString(R.string.mapsEdit_mapType_4)),
        LacMapType(5, context.getString(R.string.mapsEdit_mapType_5))
    )
}