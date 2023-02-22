package com.aliernfrog.lactool.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.FindReplace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aliernfrog.laclib.enum.LACMapOptionType
import com.aliernfrog.laclib.enum.LACMapType
import com.aliernfrog.laclib.util.DEFAULT_MAP_OBJECT_FILTERS
import com.aliernfrog.lactool.AppComponentShape
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.state.MapsEditState
import com.aliernfrog.lactool.ui.component.*
import com.aliernfrog.lactool.ui.dialog.SaveWarningDialog
import com.aliernfrog.lactool.util.Destination
import com.aliernfrog.lactool.util.extension.getName
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsEditScreen(mapsEditState: MapsEditState, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    AppScaffold(
        title = stringResource(R.string.mapsEdit),
        topAppBarState = mapsEditState.topAppBarState,
        floatingActionButton = {
            FloatingActionButton(
                icon = Icons.Rounded.Done,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                scope.launch { mapsEditState.saveAndFinishEditing(navController, context) }
            }
        },
        onBackClick = {
            scope.launch { mapsEditState.onNavigationBack(navController) }
        }
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(mapsEditState.scrollState)) {
            GeneralActions(mapsEditState, navController)
            OptionsActions(mapsEditState)
            MiscActions(mapsEditState)
            Spacer(Modifier.systemBarsPadding().height(70.dp))
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
private fun GeneralActions(mapsEditState: MapsEditState, navController: NavController) {
    val typesExpanded = remember { mutableStateOf(false) }
    ColumnDivider(title = stringResource(R.string.mapsEdit_general), bottomDivider = false) {
        AnimatedVisibilityColumn(visible = mapsEditState.mapEditor?.serverName != null) {
            TextField(
                label = stringResource(R.string.mapsEdit_serverName),
                value = mapsEditState.mapEditor?.serverName ?: "",
                onValueChange = { mapsEditState.setServerName(it) }
            )
        }
        AnimatedVisibilityColumn(visible = mapsEditState.mapEditor?.mapType != null) {
            ButtonShapeless(
                title = stringResource(R.string.mapsEdit_mapType),
                description = mapsEditState.mapEditor?.mapType?.getName() ?: "",
                expanded = typesExpanded.value
            ) {
                typesExpanded.value = !typesExpanded.value
            }
            AnimatedVisibilityColumn(visible = typesExpanded.value) {
                ColumnRounded(Modifier.padding(horizontal = 8.dp)) {
                    RadioButtons(
                        options = LACMapType.values().map { it.getName() },
                        initialIndex = (mapsEditState.mapEditor?.mapType ?: LACMapType.WHITE_GRID).index,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        optionsRounded = true,
                        onSelect = { mapsEditState.setMapType(LACMapType.values()[it]) }
                    )
                }
            }
        }
        AnimatedVisibilityColumn(visible = mapsEditState.mapEditor?.mapRoles != null) {
            ButtonShapeless(
                title = stringResource(R.string.mapsRoles),
                description = stringResource(R.string.mapsRoles_description).replace("{COUNT}", (mapsEditState.mapEditor?.mapRoles?.size ?: 0).toString()),
                expanded = false,
                arrowRotation = 90f
            ) {
                navController.navigate(Destination.MAPS_ROLES.route)
            }
        }
        AnimatedVisibilityColumn(visible = mapsEditState.mapEditor?.downloadableMaterials?.isNotEmpty() == true) {
            ButtonShapeless(
                title = stringResource(R.string.mapsMaterials),
                description = stringResource(R.string.mapsMaterials_description).replace("%n", (mapsEditState.mapEditor?.downloadableMaterials?.size ?: 0).toString()),
                expanded = false,
                arrowRotation = 90f
            ) {
                navController.navigate(Destination.MAPS_MATERIALS.route)
            }
        }
    }
}

@Composable
private fun OptionsActions(mapsEditState: MapsEditState) {
    AnimatedVisibilityColumn(visible = !mapsEditState.mapEditor?.mapOptions.isNullOrEmpty()) {
        ColumnDivider(title = stringResource(R.string.mapsEdit_options), topDivider = true, bottomDivider = false) {
            mapsEditState.mapEditor?.mapOptions?.forEach { option ->
                when (option.type) {
                    LACMapOptionType.NUMBER -> TextField(
                        label = option.label,
                        value = option.value,
                        onValueChange = {
                            option.value = it 
                            mapsEditState.updateMapEditorState()
                        },
                        placeholder = option.value,
                        numberOnly = true
                    )
                    LACMapOptionType.BOOLEAN -> Switch(
                        title = option.label,
                        checked = option.value == "true",
                        onCheckedChange = {
                            option.value = it.toString()
                            mapsEditState.updateMapEditorState()
                        }
                    )
                    LACMapOptionType.SWITCH -> Switch(
                        title = option.label,
                        checked = option.value == "enabled",
                        onCheckedChange = {
                            option.value = if (it) "enabled" else "disabled"
                            mapsEditState.updateMapEditorState()
                        }
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
        AnimatedVisibilityColumn(visible = mapsEditState.mapEditor?.replacableObjects?.isEmpty() != true) {
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
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(AppComponentShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .animateContentSize()
                    .padding(vertical = 8.dp)
            ) {
                FilterObjects(mapsEditState)
            }
        }
    }
}

@Composable
private fun FilterObjects(mapsEditState: MapsEditState) {
    val context = LocalContext.current
    val matches = mapsEditState.getObjectFilterMatches().size
    TextField(
        value = mapsEditState.objectFilter.query,
        onValueChange = { mapsEditState.objectFilter = mapsEditState.objectFilter.copy(query = it) },
        label = { Text(stringResource(R.string.mapsEdit_filterObjects_query)) },
        singleLine = true,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
    ScrollableRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        gradientColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        DEFAULT_MAP_OBJECT_FILTERS.forEach { suggestion ->
            SuggestionChip(
                onClick = { mapsEditState.objectFilter = suggestion },
                label = { Text(suggestion.filterName ?: "-") },
                shape = AppComponentShape,
                interactionSource = remember { MutableInteractionSource() }
            )
        }
    }
    Switch(
        title = stringResource(R.string.mapsEdit_filterObjects_caseSensitive),
        checked = mapsEditState.objectFilter.caseSensitive
    ) {
        mapsEditState.objectFilter = mapsEditState.objectFilter.copy(caseSensitive = it)
    }
    Switch(
        title = stringResource(R.string.mapsEdit_filterObjects_exactMatch),
        description = stringResource(R.string.mapsEdit_filterObjects_exactMatch_description),
        checked = mapsEditState.objectFilter.exactMatch
    ) {
        mapsEditState.objectFilter = mapsEditState.objectFilter.copy(exactMatch = it)
    }
    Text(
        text = stringResource(R.string.mapsEdit_filterObjects_matches).replace("%n", matches.toString()),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
    Crossfade(targetState = matches > 0) {
        ButtonCentered(
            title = stringResource(R.string.mapsEdit_filterObjects_removeMatches),
            enabled = it,
            containerColor = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 8.dp)
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