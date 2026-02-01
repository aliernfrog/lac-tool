package com.aliernfrog.lactool.ui.screen.maps

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.FindReplace
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Terrain
import androidx.compose.material.icons.rounded.Texture
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aliernfrog.laclib.enum.LACMapOptionType
import com.aliernfrog.laclib.enum.LACMapType
import com.aliernfrog.laclib.util.DEFAULT_MAP_OBJECT_FILTERS
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.laclib.MapEditorState
import com.aliernfrog.lactool.ui.component.ChipIcon
import com.aliernfrog.lactool.ui.component.AnimatedVisibilityShadowWorkaround
import com.aliernfrog.lactool.ui.component.ScrollableRow
import com.aliernfrog.lactool.ui.dialog.SaveWarningDialog
import com.aliernfrog.lactool.ui.viewmodel.MapsEditViewModel
import com.aliernfrog.lactool.util.SubDestination
import com.aliernfrog.lactool.util.extension.getName
import com.aliernfrog.lactool.util.staticutil.FileUtil
import io.github.aliernfrog.pftool_shared.impl.Progress
import io.github.aliernfrog.pftool_shared.ui.component.VerticalProgressIndicatorWithText
import io.github.aliernfrog.shared.ui.component.AppScaffold
import io.github.aliernfrog.shared.ui.component.AppTopBar
import io.github.aliernfrog.shared.ui.component.FadeVisibility
import io.github.aliernfrog.shared.ui.component.FloatingActionButton
import io.github.aliernfrog.shared.ui.component.RadioButtons
import io.github.aliernfrog.shared.ui.component.SingleChoiceConnectedButtonGroup
import io.github.aliernfrog.shared.ui.component.VerticalSegmentor
import io.github.aliernfrog.shared.ui.component.expressive.ExpressiveButtonRow
import io.github.aliernfrog.shared.ui.component.expressive.ExpressiveRowIcon
import io.github.aliernfrog.shared.ui.component.expressive.ExpressiveSection
import io.github.aliernfrog.shared.ui.component.expressive.ExpressiveSwitchRow
import io.github.aliernfrog.shared.ui.component.expressive.getTextFieldColors
import io.github.aliernfrog.shared.ui.component.form.ExpandableRow
import io.github.aliernfrog.shared.ui.component.form.getExpandableRowDefaultExpandedContainerColor
import io.github.aliernfrog.shared.ui.component.util.ScrollAccessibilityListener
import io.github.aliernfrog.shared.ui.theme.AppFABPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsEditScreen(
    vm: MapsEditViewModel,
    vmKey: String,
    onNavigateRequest: (Any) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showFABLabel by remember { mutableStateOf(true) }

    BackHandler {
        vm.showSaveWarning()
    }

    ScrollAccessibilityListener(
        scrollState = vm.scrollState,
        onShowLabelsStateChange = { showFABLabel = it }
    )

    AppScaffold(
        topBar = { scrollBehavior ->
            AppTopBar(
                title = stringResource(R.string.mapsEdit),
                scrollBehavior = scrollBehavior,
                onNavigationClick = { scope.launch {
                    vm.showSaveWarning()
                } }
            )
        },
        topAppBarState = vm.topAppBarState,
        floatingActionButton = {
            AnimatedVisibilityShadowWorkaround(
                visible = vm.mapEditor != null,
                modifier = Modifier.navigationBarsPadding()
            ) {
                FloatingActionButton(
                    icon = Icons.Default.Save,
                    text = stringResource(R.string.mapsEdit_save),
                    showText = showFABLabel
                ) {
                    scope.launch {
                        vm.saveAndFinishEditing(context)
                    }
                }
            }
        }
    ) {
        AnimatedContent(
            targetState = vm.mapEditor,
            modifier = Modifier.fillMaxSize()
        ) { editor ->
            if (editor == null) VerticalProgressIndicatorWithText(
                progress = Progress(
                    description = stringResource(R.string.mapsEdit_opening)
                        .replace("{NAME}", vm.map.name)
                )
            )
            else Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(vm.scrollState)
            ) {
                GeneralActions(
                    vm = vm,
                    vmKey = vmKey,
                    mapEditor = editor,
                    onNavigateRequest = onNavigateRequest
                )
                FadeVisibility(
                    visible = editor.mapOptions.isNotEmpty()
                ) {
                    OptionsActions(editor)
                }
                MiscActions(vm, editor)
                Spacer(Modifier.navigationBarsPadding().height(AppFABPadding))
            }
        }

    }

    if (vm.saveWarningShown) SaveWarningDialog(
        onDismissRequest = { vm.saveWarningShown = false },
        onKeepEditing = { vm.saveWarningShown = false },
        onDiscardChanges = {
            scope.launch {
                vm.finishEditingWithoutSaving()
                vm.saveWarningShown = false
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GeneralActions(
    vm: MapsEditViewModel,
    vmKey: String,
    mapEditor: MapEditorState,
    onNavigateRequest: (SubDestination) -> Unit
) {
    ExpressiveSection(title = stringResource(R.string.mapsEdit_general)) {
        VerticalSegmentor(
            {
                FadeVisibility(
                    visible = mapEditor.serverName != null
                ) {
                    TextField(
                        value = mapEditor.serverName ?: "",
                        onValueChange = {
                            vm.setServerName(it)
                        },
                        label = {
                            Text(stringResource(R.string.mapsEdit_serverName))
                        },
                        leadingIcon = {
                            ExpressiveRowIcon(
                                painter = rememberVectorPainter(Icons.Rounded.Info),
                                modifier = Modifier.padding(start = 18.dp, end = 12.dp)
                            )
                        },
                        singleLine = true,
                        colors = getTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            {
                FadeVisibility(
                    visible = mapEditor.mapType != null
                ) {
                    ExpandableRow(
                        expanded = vm.mapTypesExpanded,
                        title = stringResource(R.string.mapsEdit_mapType),
                        minimizedHeaderTrailingButtonText = mapEditor.mapType?.getName() ?: "",
                        icon = {
                            ExpressiveRowIcon(rememberVectorPainter(Icons.Rounded.Terrain))
                        },
                        onClickHeader = {
                            vm.mapTypesExpanded = !vm.mapTypesExpanded
                        }
                    ) {
                        RadioButtons(
                            options = LACMapType.entries.map { it.getName() },
                            selectedOptionIndex = (mapEditor.mapType ?: LACMapType.WHITE_GRID).index,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            onSelect = { vm.setMapType(LACMapType.entries[it]) },
                            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                        )
                    }
                }
            },
            {
                FadeVisibility(
                    visible = mapEditor.mapRoles != null
                ) {
                    val onClick: () -> Unit = {
                        onNavigateRequest(
                            SubDestination.MapsEdit.Roles(vmKey = vmKey)
                        )
                    }
                    ExpressiveButtonRow(
                        title = stringResource(R.string.mapsRoles),
                        description = stringResource(R.string.mapsRoles_description)
                            .replace("{COUNT}", (mapEditor.mapRoles?.size ?: 0).toString()),
                        icon = {
                            ExpressiveRowIcon(rememberVectorPainter(Icons.Rounded.Shield))
                        },
                        trailingComponent = {
                            FilledTonalIconButton(
                                shapes = IconButtonDefaults.shapes(),
                                onClick = onClick
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null
                                )
                            }
                        },
                        onClick = onClick
                    )
                }
            },
            {
                FadeVisibility(
                    visible = mapEditor.downloadableMaterials.isNotEmpty()
                ) {
                    val onClick = {
                        onNavigateRequest(
                            SubDestination.MapsEdit.Materials(vmKey = vmKey)
                        )
                    }
                    ExpressiveButtonRow(
                        title = stringResource(R.string.mapsMaterials),
                        description = stringResource(R.string.mapsMaterials_description)
                            .replace("%n", mapEditor.downloadableMaterials.size.toString()),
                        icon = {
                            ExpressiveRowIcon(rememberVectorPainter(Icons.Rounded.Texture))
                        },
                        trailingComponent = {
                            FilledTonalIconButton(
                                shapes = IconButtonDefaults.shapes(),
                                onClick = onClick
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null
                                )
                            }
                        },
                        onClick = onClick
                    )
                }
            },
            dynamic = true,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun OptionsActions(
    mapEditor: MapEditorState
) {
    val optionsComponents: List<@Composable () -> Unit> = mapEditor.mapOptions.map { option -> {
        when (option.type) {
            LACMapOptionType.NUMBER -> TextField(
                value = option.value,
                onValueChange = {
                    option.value = it
                    mapEditor.pushMapOptionsState()
                },
                label = {
                    Text(option.label)
                },
                isError = option.value.toIntOrNull() == null,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    autoCorrectEnabled = null
                ),
                colors = getTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            LACMapOptionType.BOOLEAN -> ExpressiveSwitchRow(
                title = option.label,
                checked = option.value == "true",
                onCheckedChange = {
                    option.value = it.toString()
                    mapEditor.pushMapOptionsState()
                }
            )
            LACMapOptionType.SWITCH -> ExpressiveSwitchRow(
                title = option.label,
                checked = option.value == "enabled",
                onCheckedChange = {
                    option.value = if (it) "enabled" else "disabled"
                    mapEditor.pushMapOptionsState()
                }
            )
        }
    } }

    ExpressiveSection(title = stringResource(R.string.mapsEdit_options)) {
        VerticalSegmentor(
            *optionsComponents.toTypedArray(),
            dynamic = true,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun MiscActions(
    vm: MapsEditViewModel,
    mapEditor: MapEditorState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val debugBadge: @Composable () -> Unit = {
        Text("DEBUG")
    }
    val debugButtons: List<@Composable () -> Unit> = if (vm.prefs.debug.value) listOf(
        {
            ExpressiveButtonRow(
                title = "View getCurrentContent()",
                description = "without applyChanges()",
                trailingComponent = debugBadge
            ) {
                scope.launch(Dispatchers.IO) {
                    FileUtil.openTextAsFile(
                        text = mapEditor.editor.getCurrentContent(),
                        context = context
                    )
                }
            }
        },
        {
            ExpressiveButtonRow(
                title = "applyChanges() and view result",
                description = "This will not save to file",
                trailingComponent = debugBadge
            ) {
                scope.launch(Dispatchers.IO) {
                    FileUtil.openTextAsFile(
                        text = mapEditor.editor.applyChanges(),
                        context = context
                    )
                }
            }
        }
    ) else listOf()

    ExpressiveSection(stringResource(R.string.mapsEdit_misc)) {
        VerticalSegmentor(
            {
                FadeVisibility(
                    visible = !mapEditor.replaceableObjects.isEmpty()
                ) {
                    ExpressiveButtonRow(
                        title = stringResource(R.string.mapsEdit_misc_replaceOldObjects),
                        description = stringResource(R.string.mapsEdit_misc_replaceOldObjects_description),
                        icon = {
                            ExpressiveRowIcon(
                                painter = rememberVectorPainter(Icons.Rounded.FindReplace)
                            )
                        }
                    ) {
                        vm.replaceOldObjects(context)
                    }
                }
            },
            {
                val expandedRowColor = getExpandableRowDefaultExpandedContainerColor(fraction = 0.1f)
                ExpandableRow(
                    expanded = vm.objectFilterExpanded,
                    title = stringResource(R.string.mapsEdit_filterObjects),
                    description = stringResource(R.string.mapsEdit_filterObjects_description),
                    icon = {
                        ExpressiveRowIcon(
                            painter = rememberVectorPainter(Icons.Rounded.FilterAlt)
                        )
                    },
                    expandedContainerColor = expandedRowColor,
                    onClickHeader = {
                        vm.objectFilterExpanded = !vm.objectFilterExpanded
                    }
                ) {
                    Column(Modifier.padding(vertical = 8.dp)) {
                        FilterObjects(
                            vm = vm,
                            containerColor = expandedRowColor
                        )
                    }
                }
            },
            *debugButtons.toTypedArray(),
            dynamic = true,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FilterObjects(
    containerColor: Color,
    vm: MapsEditViewModel
) {
    val context = LocalContext.current
    val matches = vm.getObjectFilterMatches().size
    OutlinedTextField(
        value = vm.objectFilter.query,
        onValueChange = {
            vm.objectFilter = vm.objectFilter.copy(query = it)
        },
        label = { Text(stringResource(R.string.mapsEdit_filterObjects_query)) },
        trailingIcon = {
            Icon(Icons.Default.Search, null)
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 12.dp,
                end = 12.dp,
                bottom = 4.dp
            )
    )
    ScrollableRow(
        modifier = Modifier.padding(horizontal = 12.dp),
        gradientColor = containerColor
    ) {
        DEFAULT_MAP_OBJECT_FILTERS.forEach { suggestion ->
            FilterChip(
                selected = vm.objectFilter == suggestion,
                onClick = { vm.objectFilter = suggestion },
                label = { Text(suggestion.filterName ?: "-") }
            )
        }
    }

    FilterChip(
        selected = vm.objectFilter.caseSensitive,
        onClick = {
            vm.objectFilter = vm.objectFilter.copy(
                caseSensitive = !vm.objectFilter.caseSensitive
            )
        },
        label = {
            Text(stringResource(R.string.mapsEdit_filterObjects_caseSensitive))
        },
        leadingIcon = if (vm.objectFilter.caseSensitive) { {
            ChipIcon(
                painter = rememberVectorPainter(Icons.Default.Check)
            )
        } } else null,
        modifier = Modifier.padding(horizontal = 12.dp)
    )

    SingleChoiceConnectedButtonGroup(
        choices = listOf(
            // The order here is important as checks are hardcoded
            stringResource(R.string.mapsEdit_filterObjects_filter_startsWith), // 0
            stringResource(R.string.mapsEdit_filterObjects_filter_exact) // 1
        ),
        selectedIndex = if (vm.objectFilter.exactMatch) 1 else 0,
        onSelect = {
            vm.objectFilter = vm.objectFilter.copy(
                exactMatch = it == 1
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    )

    Text(
        text = stringResource(R.string.mapsEdit_filterObjects_matches).replace("%n", matches.toString()),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    )

    Crossfade(targetState = matches > 0) {
        Button(
            onClick = { vm.removeObjectFilterMatches(context) },
            shapes = ButtonDefaults.shapes(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            enabled = it,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
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