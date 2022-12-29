package com.aliernfrog.lactool.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.FindReplace
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aliernfrog.lactool.AppComposableShape
import com.aliernfrog.lactool.LACMapObjectFilters
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LACMapType
import com.aliernfrog.lactool.enum.LACMapOptionType
import com.aliernfrog.lactool.state.MapsEditState
import com.aliernfrog.lactool.ui.component.*
import com.aliernfrog.lactool.ui.dialog.SaveWarningDialog
import com.aliernfrog.lactool.util.Destination
import kotlinx.coroutines.launch

@Composable
fun MapsEditScreen(mapsEditState: MapsEditState, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize()) {
        Actions(mapsEditState, navController)
        FloatingActionButton(
            icon = Icons.Rounded.Done,
            modifier = Modifier.align(Alignment.BottomEnd),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            scope.launch { mapsEditState.saveAndFinishEditing(navController, context) }
        }
    }
    if (mapsEditState.saveWarningShown.value) SaveWarningDialog(
        onDismissRequest = { mapsEditState.saveWarningShown.value = false },
        onKeepEditing = { mapsEditState.saveWarningShown.value = false },
        onDiscardChanges = {
            scope.launch {
                mapsEditState.finishEditingWithoutSaving(navController)
                mapsEditState.saveWarningShown.value = false
            }
        }
    )
    BackHandler {
        scope.launch { mapsEditState.onNavigationBack(navController) }
    }
}

@Composable
private fun Actions(mapsEditState: MapsEditState, navController: NavController) {
    Column(Modifier.animateContentSize().verticalScroll(mapsEditState.scrollState)) {
        GeneralActions(mapsEditState, navController)
        OptionsActions(mapsEditState)
        MiscActions(mapsEditState)
        Spacer(Modifier.height(70.dp))
    }
}

@Composable
private fun GeneralActions(mapsEditState: MapsEditState, navController: NavController) {
    val typesExpanded = remember { mutableStateOf(false) }
    ColumnDivider(title = stringResource(R.string.mapsEdit_general), bottomDivider = false) {
        AnimatedVisibilityColumn(visible = mapsEditState.mapData.value?.serverName != null) {
            TextField(
                label = stringResource(R.string.mapsEdit_serverName),
                value = mapsEditState.mapData.value?.serverName?.value ?: "",
                onValueChange = { mapsEditState.mapData.value?.serverName?.value = it }
            )
        }
        AnimatedVisibilityColumn(visible = mapsEditState.mapData.value?.mapType != null) {
            ButtonShapeless(
                title = stringResource(R.string.mapsEdit_mapType),
                description = getMapTypes().find { it.index == mapsEditState.mapData.value?.mapType?.value }?.label ?: "unknown",
                expanded = typesExpanded.value
            ) {
                typesExpanded.value = !typesExpanded.value
            }
            AnimatedVisibilityColumn(visible = typesExpanded.value) {
                ColumnRounded(Modifier.padding(horizontal = 8.dp)) {
                    RadioButtons(
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
            ButtonShapeless(
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
        ColumnDivider(title = stringResource(R.string.mapsEdit_options), topDivider = true, bottomDivider = false) {
            mapsEditState.mapData.value?.mapOptions?.forEach { option ->
                when (option.type) {
                    LACMapOptionType.NUMBER -> TextField(
                        label = option.label,
                        value = option.value.value,
                        onValueChange = { option.value.value = it },
                        placeholder = option.value.value,
                        numberOnly = true
                    )
                    LACMapOptionType.BOOLEAN -> Switch(
                        title = option.label,
                        checked = option.value.value == "true",
                        onCheckedChange = { option.value.value = it.toString() }
                    )
                    LACMapOptionType.SWITCH -> Switch(
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
private fun MiscActions(mapsEditState: MapsEditState) {
    val context = LocalContext.current
    var filterObjectsExpanded by remember { mutableStateOf(false) }
    ColumnDivider(title = stringResource(R.string.mapsEdit_misc), topDivider = true, bottomDivider = false) {
        AnimatedVisibilityColumn(visible = mapsEditState.mapData.value?.replacableObjects?.isEmpty() != true) {
            ButtonShapeless(
                title = stringResource(R.string.mapsEdit_misc_replaceOldObjects),
                description = stringResource(R.string.mapsEdit_misc_replaceOldObjects_description),
                painter = rememberVectorPainter(Icons.Rounded.FindReplace)
            ) {
                mapsEditState.replaceOldObjects(context)
            }
        }
        ButtonShapeless(
            title = stringResource(R.string.mapsEdit_filterObjects),
            description = stringResource(R.string.mapsEdit_filterObjects_description),
            painter = rememberVectorPainter(Icons.Rounded.FilterAlt),
            expanded = filterObjectsExpanded
        ) {
            filterObjectsExpanded = !filterObjectsExpanded
        }
        AnimatedVisibilityColumn(visible = filterObjectsExpanded) {
            ColumnRounded(Modifier.padding(horizontal = 8.dp)) {
                FilterObjects(mapsEditState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterObjects(mapsEditState: MapsEditState) {
    val context = LocalContext.current
    val matches = mapsEditState.getObjectFilterMatches().size
    TextField(
        value = mapsEditState.objectFilter.value.query.value,
        onValueChange = { mapsEditState.objectFilter.value.query.value = it },
        label = { Text(stringResource(R.string.mapsEdit_filterObjects_query)) },
        singleLine = true
    )
    Row(
        modifier = Modifier.padding(horizontal = 8.dp).horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LACMapObjectFilters.defaultFilters.forEach { suggestion ->
            SuggestionChip(
                onClick = { mapsEditState.setObjectFilterFromSuggestion(suggestion) },
                label = { Text(stringResource(suggestion.labelStringId!!)) },
                shape = AppComposableShape,
                interactionSource = remember { MutableInteractionSource() }
            )
        }
    }
    Switch(
        title = stringResource(R.string.mapsEdit_filterObjects_caseSensitive),
        checked = mapsEditState.objectFilter.value.caseSensitive.value,
        rounded = true
    ) {
        mapsEditState.objectFilter.value.caseSensitive.value = it
    }
    Switch(
        title = stringResource(R.string.mapsEdit_filterObjects_exactMatch),
        description = stringResource(R.string.mapsEdit_filterObjects_exactMatch_description),
        checked = mapsEditState.objectFilter.value.exactMatch.value,
        rounded = true,
    ) {
        mapsEditState.objectFilter.value.exactMatch.value = it
    }
    Text(
        text = stringResource(R.string.mapsEdit_filterObjects_matches).replace("%n", matches.toString()),
        modifier = Modifier.padding(8.dp)
    )
    Crossfade(targetState = matches > 0) {
        ButtonCentered(
            title = stringResource(R.string.mapsEdit_filterObjects_removeMatches),
            enabled = it,
            containerColor = MaterialTheme.colorScheme.error
        ) {
            mapsEditState.removeObjectFilterMatches(context)
        }
    }
}

@Composable
private fun TextField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String? = null, numberOnly: Boolean = false) {
    TextField(
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
private fun getMapTypes(): List<LACMapType> {
    return listOf(
        LACMapType(0, stringResource(R.string.mapsEdit_mapType_0)),
        LACMapType(1, stringResource(R.string.mapsEdit_mapType_1)),
        LACMapType(2, stringResource(R.string.mapsEdit_mapType_2)),
        LACMapType(3, stringResource(R.string.mapsEdit_mapType_3)),
        LACMapType(4, stringResource(R.string.mapsEdit_mapType_4)),
        LACMapType(5, stringResource(R.string.mapsEdit_mapType_5))
    )
}