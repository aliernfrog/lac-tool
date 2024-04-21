package com.aliernfrog.lactool.ui.screen.maps

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.FindReplace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.aliernfrog.laclib.enum.LACMapOptionType
import com.aliernfrog.laclib.enum.LACMapType
import com.aliernfrog.laclib.util.DEFAULT_MAP_OBJECT_FILTERS
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.*
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.component.form.ExpandableRow
import com.aliernfrog.lactool.ui.component.form.FormSection
import com.aliernfrog.lactool.ui.component.form.SwitchRow
import com.aliernfrog.lactool.ui.dialog.SaveWarningDialog
import com.aliernfrog.lactool.ui.viewmodel.MapsEditViewModel
import com.aliernfrog.lactool.util.Destination
import com.aliernfrog.lactool.util.extension.getName
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsEditScreen(
    mapsEditViewModel: MapsEditViewModel = koinViewModel(),
    onNavigateBackRequest: () -> Unit,
    onNavigateRequest: (Destination) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    AppScaffold(
        topBar = { scrollBehavior ->
            AppTopBar(
                title = stringResource(R.string.mapsEdit),
                scrollBehavior = scrollBehavior,
                onNavigationClick = { scope.launch {
                    mapsEditViewModel.onNavigationBack(onNavigateBackRequest)
                } }
            )
        },
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
    mapsEditViewModel: MapsEditViewModel = koinViewModel(),
    onNavigateRequest: (Destination) -> Unit
) {
    FormSection(title = stringResource(R.string.mapsEdit_general), bottomDivider = false) {
        FadeVisibilityColumn(visible = mapsEditViewModel.mapEditor?.serverName != null) {
            TextField(
                label = stringResource(R.string.mapsEdit_serverName),
                value = mapsEditViewModel.mapEditor?.serverName ?: "",
                onValueChange = { mapsEditViewModel.setServerName(it) }
            )
        }
        FadeVisibilityColumn(visible = mapsEditViewModel.mapEditor?.mapType != null) {
            ExpandableRow(
                expanded = mapsEditViewModel.mapTypesExpanded,
                title = stringResource(R.string.mapsEdit_mapType),
                description = mapsEditViewModel.mapEditor?.mapType?.getName() ?: "",
                onClickHeader = {
                    mapsEditViewModel.mapTypesExpanded = !mapsEditViewModel.mapTypesExpanded
                }
            ) {
                RadioButtons(
                    options = LACMapType.entries.map { it.getName() },
                    selectedOptionIndex = (mapsEditViewModel.mapEditor?.mapType ?: LACMapType.WHITE_GRID).index,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onSelect = { mapsEditViewModel.setMapType(LACMapType.entries[it]) }
                )
            }
        }
        FadeVisibilityColumn(visible = mapsEditViewModel.mapEditor?.mapRoles != null) {
            ButtonRow(
                title = stringResource(R.string.mapsRoles),
                description = stringResource(R.string.mapsRoles_description)
                    .replace("{COUNT}", (mapsEditViewModel.mapEditor?.mapRoles?.size ?: 0).toString()),
                expanded = false,
                arrowRotation = if (LocalLayoutDirection.current == LayoutDirection.Rtl) 270f else 90f
            ) {
                onNavigateRequest(Destination.MAPS_ROLES)
            }
        }
        FadeVisibilityColumn(visible = mapsEditViewModel.mapEditor?.downloadableMaterials?.isNotEmpty() == true) {
            ButtonRow(
                title = stringResource(R.string.mapsMaterials),
                description = stringResource(R.string.mapsMaterials_description)
                    .replace("%n", (mapsEditViewModel.mapEditor?.downloadableMaterials?.size ?: 0).toString()),
                expanded = false,
                arrowRotation = if (LocalLayoutDirection.current == LayoutDirection.Rtl) 270f else 90f
            ) {
                onNavigateRequest(Destination.MAPS_MATERIALS)
            }
        }
    }
}

@Composable
private fun OptionsActions(
    mapsEditViewModel: MapsEditViewModel = koinViewModel()
) {
    FadeVisibilityColumn(visible = !mapsEditViewModel.mapEditor?.mapOptions.isNullOrEmpty()) {
        FormSection(title = stringResource(R.string.mapsEdit_options), topDivider = true, bottomDivider = false) {
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
                    LACMapOptionType.BOOLEAN -> SwitchRow(
                        title = option.label,
                        checked = option.value == "true",
                        onCheckedChange = {
                            option.value = it.toString()
                            mapsEditViewModel.updateMapEditorState()
                        }
                    )
                    LACMapOptionType.SWITCH -> SwitchRow(
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
    mapsEditViewModel: MapsEditViewModel = koinViewModel()
) {
    val context = LocalContext.current
    FormSection(title = stringResource(R.string.mapsEdit_misc), topDivider = true, bottomDivider = false) {
        FadeVisibilityColumn(visible = mapsEditViewModel.mapEditor?.replacableObjects?.isEmpty() != true) {
            ButtonRow(
                title = stringResource(R.string.mapsEdit_misc_replaceOldObjects),
                description = stringResource(R.string.mapsEdit_misc_replaceOldObjects_description),
                painter = rememberVectorPainter(Icons.Rounded.FindReplace)
            ) {
                mapsEditViewModel.replaceOldObjects(context)
            }
        }
        ExpandableRow(
            expanded = mapsEditViewModel.objectFilterExpanded,
            title = stringResource(R.string.mapsEdit_filterObjects),
            description = stringResource(R.string.mapsEdit_filterObjects_description),
            painter = rememberVectorPainter(Icons.Rounded.FilterAlt),
            onClickHeader = {
                mapsEditViewModel.objectFilterExpanded = !mapsEditViewModel.objectFilterExpanded
            }
        ) {
            Column(Modifier.padding(vertical = 8.dp)) {
                FilterObjects()
            }
        }
    }
}

@Composable
private fun FilterObjects(
    mapsEditViewModel: MapsEditViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val matches = mapsEditViewModel.getObjectFilterMatches().size
    OutlinedTextField(
        value = mapsEditViewModel.objectFilter.query,
        onValueChange = {
            mapsEditViewModel.objectFilter = mapsEditViewModel.objectFilter.copy(query = it)
        },
        label = { Text(stringResource(R.string.mapsEdit_filterObjects_query)) },
        trailingIcon = {
            Icon(Icons.Default.Search, null)
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 4.dp
            )
    )
    ScrollableRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        gradientColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        DEFAULT_MAP_OBJECT_FILTERS.forEach { suggestion ->
            SuggestionChip(
                onClick = { mapsEditViewModel.objectFilter = suggestion },
                label = { Text(suggestion.filterName ?: "-") }
            )
        }
    }
    SwitchRow(
        title = stringResource(R.string.mapsEdit_filterObjects_caseSensitive),
        checked = mapsEditViewModel.objectFilter.caseSensitive
    ) {
        mapsEditViewModel.objectFilter = mapsEditViewModel.objectFilter.copy(caseSensitive = it)
    }
    SwitchRow(
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
        Button(
            onClick = { mapsEditViewModel.removeObjectFilterMatches(context) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            enabled = it,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(stringResource(R.string.mapsEdit_filterObjects_removeMatches))
        }
    }
}

@Composable
private fun TextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    numberOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder != null) { { Text(placeholder) } } else null,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = if (numberOnly) KeyboardType.Number else KeyboardType.Text,
            autoCorrectEnabled = null
        ),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 4.dp
            )
    )
}