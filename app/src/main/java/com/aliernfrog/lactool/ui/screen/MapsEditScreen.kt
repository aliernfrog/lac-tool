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
import com.aliernfrog.laclib.enum.LACMapOptionType
import com.aliernfrog.laclib.enum.LACMapType
import com.aliernfrog.laclib.util.DEFAULT_MAP_OBJECT_FILTERS
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.*
import com.aliernfrog.lactool.ui.dialog.SaveWarningDialog
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.ui.viewmodel.MapsEditViewModel
import com.aliernfrog.lactool.util.Destination
import com.aliernfrog.lactool.util.extension.getName
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsEditScreen(
    mapsEditViewModel: MapsEditViewModel = getViewModel(),
    onNavigateBackRequest: () -> Unit,
    onNavigateRequest: (Destination) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    AppScaffold(
        title = stringResource(R.string.mapsEdit),
        topAppBarState = mapsEditViewModel.topAppBarState,
        floatingActionButton = {
            FloatingActionButton(
                icon = Icons.Rounded.Done,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                scope.launch {
                    mapsEditViewModel.saveAndFinishEditing(
                        onNavigateBackRequest = onNavigateBackRequest,
                        context = context
                    )
                }
            }
        },
        onBackClick = {
            scope.launch { mapsEditViewModel.onNavigationBack(onNavigateBackRequest) }
        }
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(mapsEditViewModel.scrollState)) {
            GeneralActions(
                onNavigateRequest = onNavigateRequest
            )
            OptionsActions()
            MiscActions()
            Spacer(Modifier.systemBarsPadding().height(70.dp))
        }
    }
    if (mapsEditViewModel.saveWarningShown) SaveWarningDialog(
        onDismissRequest = { mapsEditViewModel.saveWarningShown = false },
        onKeepEditing = { mapsEditViewModel.saveWarningShown = false },
        onDiscardChanges = {
            scope.launch {
                mapsEditViewModel.finishEditingWithoutSaving(onNavigateBackRequest)
                mapsEditViewModel.saveWarningShown = false
            }
        }
    )
    BackHandler {
        scope.launch { mapsEditViewModel.onNavigationBack(onNavigateBackRequest) }
    }
}

@Composable
private fun GeneralActions(
    mapsEditViewModel: MapsEditViewModel = getViewModel(),
    onNavigateRequest: (Destination) -> Unit
) {
    val typesExpanded = remember { mutableStateOf(false) }
    ColumnDivider(title = stringResource(R.string.mapsEdit_general), bottomDivider = false) {
        AnimatedVisibilityColumn(visible = mapsEditViewModel.mapEditor?.serverName != null) {
            TextField(
                label = stringResource(R.string.mapsEdit_serverName),
                value = mapsEditViewModel.mapEditor?.serverName ?: "",
                onValueChange = { mapsEditViewModel.setServerName(it) }
            )
        }
        AnimatedVisibilityColumn(visible = mapsEditViewModel.mapEditor?.mapType != null) {
            ButtonShapeless(
                title = stringResource(R.string.mapsEdit_mapType),
                description = mapsEditViewModel.mapEditor?.mapType?.getName() ?: "",
                expanded = typesExpanded.value
            ) {
                typesExpanded.value = !typesExpanded.value
            }
            AnimatedVisibilityColumn(visible = typesExpanded.value) {
                ColumnRounded(Modifier.padding(horizontal = 8.dp)) {
                    RadioButtons(
                        options = LACMapType.values().map { it.getName() },
                        initialIndex = (mapsEditViewModel.mapEditor?.mapType ?: LACMapType.WHITE_GRID).index,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        optionsRounded = true,
                        onSelect = { mapsEditViewModel.setMapType(LACMapType.values()[it]) }
                    )
                }
            }
        }
        AnimatedVisibilityColumn(visible = mapsEditViewModel.mapEditor?.mapRoles != null) {
            ButtonShapeless(
                title = stringResource(R.string.mapsRoles),
                description = stringResource(R.string.mapsRoles_description)
                    .replace("{COUNT}", (mapsEditViewModel.mapEditor?.mapRoles?.size ?: 0).toString()),
                expanded = false,
                arrowRotation = 90f
            ) {
                onNavigateRequest(Destination.MAPS_ROLES)
            }
        }
        AnimatedVisibilityColumn(visible = mapsEditViewModel.mapEditor?.downloadableMaterials?.isNotEmpty() == true) {
            ButtonShapeless(
                title = stringResource(R.string.mapsMaterials),
                description = stringResource(R.string.mapsMaterials_description)
                    .replace("%n", (mapsEditViewModel.mapEditor?.downloadableMaterials?.size ?: 0).toString()),
                expanded = false,
                arrowRotation = 90f
            ) {
                onNavigateRequest(Destination.MAPS_MATERIALS)
            }
        }
    }
}

@Composable
private fun OptionsActions(
    mapsEditViewModel: MapsEditViewModel = getViewModel()
) {
    AnimatedVisibilityColumn(visible = !mapsEditViewModel.mapEditor?.mapOptions.isNullOrEmpty()) {
        ColumnDivider(title = stringResource(R.string.mapsEdit_options), topDivider = true, bottomDivider = false) {
            mapsEditViewModel.mapEditor?.mapOptions?.forEach { option ->
                when (option.type) {
                    LACMapOptionType.NUMBER -> TextField(
                        label = option.label,
                        value = option.value,
                        onValueChange = {
                            option.value = it 
                            mapsEditViewModel.updateMapEditorState()
                        },
                        placeholder = option.value,
                        numberOnly = true
                    )
                    LACMapOptionType.BOOLEAN -> Switch(
                        title = option.label,
                        checked = option.value == "true",
                        onCheckedChange = {
                            option.value = it.toString()
                            mapsEditViewModel.updateMapEditorState()
                        }
                    )
                    LACMapOptionType.SWITCH -> Switch(
                        title = option.label,
                        checked = option.value == "enabled",
                        onCheckedChange = {
                            option.value = if (it) "enabled" else "disabled"
                            mapsEditViewModel.updateMapEditorState()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MiscActions(
    mapsEditViewModel: MapsEditViewModel = getViewModel()
) {
    val context = LocalContext.current
    var filterObjectsExpanded by remember { mutableStateOf(false) }
    ColumnDivider(title = stringResource(R.string.mapsEdit_misc), topDivider = true, bottomDivider = false) {
        AnimatedVisibilityColumn(visible = mapsEditViewModel.mapEditor?.replacableObjects?.isEmpty() != true) {
            ButtonShapeless(
                title = stringResource(R.string.mapsEdit_misc_replaceOldObjects),
                description = stringResource(R.string.mapsEdit_misc_replaceOldObjects_description),
                painter = rememberVectorPainter(Icons.Rounded.FindReplace)
            ) {
                mapsEditViewModel.replaceOldObjects(context)
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
                FilterObjects()
            }
        }
    }
}

@Composable
private fun FilterObjects(
    mapsEditViewModel: MapsEditViewModel = getViewModel()
) {
    val context = LocalContext.current
    val matches = mapsEditViewModel.getObjectFilterMatches().size
    TextField(
        value = mapsEditViewModel.objectFilter.query,
        onValueChange = {
            mapsEditViewModel.objectFilter = mapsEditViewModel.objectFilter.copy(query = it)
        },
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
                onClick = { mapsEditViewModel.objectFilter = suggestion },
                label = { Text(suggestion.filterName ?: "-") },
                shape = AppComponentShape,
                interactionSource = remember { MutableInteractionSource() }
            )
        }
    }
    Switch(
        title = stringResource(R.string.mapsEdit_filterObjects_caseSensitive),
        checked = mapsEditViewModel.objectFilter.caseSensitive
    ) {
        mapsEditViewModel.objectFilter = mapsEditViewModel.objectFilter.copy(caseSensitive = it)
    }
    Switch(
        title = stringResource(R.string.mapsEdit_filterObjects_exactMatch),
        description = stringResource(R.string.mapsEdit_filterObjects_exactMatch_description),
        checked = mapsEditViewModel.objectFilter.exactMatch
    ) {
        mapsEditViewModel.objectFilter = mapsEditViewModel.objectFilter.copy(exactMatch = it)
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
            mapsEditViewModel.removeObjectFilterMatches(context)
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
        rounded = false,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
private fun AnimatedVisibilityColumn(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(visible, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
        Column { content() }
    }
}