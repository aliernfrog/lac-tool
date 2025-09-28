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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Done
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
import com.aliernfrog.lactool.ui.component.*
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveButtonRow
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveRowIcon
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveSection
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveSwitchRow
import com.aliernfrog.lactool.ui.component.form.ExpandableRow
import com.aliernfrog.lactool.ui.component.form.getExpandableRowDefaultExpandedContainerColor
import com.aliernfrog.lactool.ui.dialog.SaveWarningDialog
import com.aliernfrog.lactool.ui.theme.AppFABPadding
import com.aliernfrog.lactool.ui.viewmodel.MapsEditViewModel
import com.aliernfrog.lactool.util.Destination
import com.aliernfrog.lactool.util.extension.getName
import com.aliernfrog.lactool.util.staticutil.FileUtil
import kotlinx.coroutines.Dispatchers
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
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(mapsEditViewModel.scrollState)
        ) {
            GeneralActions(
                onNavigateRequest = onNavigateRequest
            )
            FadeVisibility(visible = !mapsEditViewModel.mapEditor?.mapOptions.isNullOrEmpty()) {
                OptionsActions()
            }
            MiscActions()
            Spacer(Modifier
                .navigationBarsPadding()
                .height(AppFABPadding))
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GeneralActions(
    mapsEditViewModel: MapsEditViewModel = koinViewModel(),
    onNavigateRequest: (Destination) -> Unit
) {
    ExpressiveSection(title = stringResource(R.string.mapsEdit_general)) {
        VerticalSegmentor(
            {
                FadeVisibility(visible = mapsEditViewModel.mapEditor?.serverName != null) {
                    TextField(
                        value = mapsEditViewModel.mapEditor?.serverName ?: "",
                        onValueChange = {
                            mapsEditViewModel.setServerName(it)
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
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            {
                FadeVisibility(visible = mapsEditViewModel.mapEditor?.mapType != null) {
                    ExpandableRow(
                        expanded = mapsEditViewModel.mapTypesExpanded,
                        title = stringResource(R.string.mapsEdit_mapType),
                        minimizedHeaderTrailingButtonText = mapsEditViewModel.mapEditor?.mapType?.getName() ?: "",
                        icon = {
                            ExpressiveRowIcon(rememberVectorPainter(Icons.Rounded.Terrain))
                        },
                        onClickHeader = {
                            mapsEditViewModel.mapTypesExpanded = !mapsEditViewModel.mapTypesExpanded
                        }
                    ) {
                        RadioButtons(
                            options = LACMapType.entries.map { it.getName() },
                            selectedOptionIndex = (mapsEditViewModel.mapEditor?.mapType ?: LACMapType.WHITE_GRID).index,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            onSelect = { mapsEditViewModel.setMapType(LACMapType.entries[it]) },
                            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                        )
                    }
                }
            },
            {
                FadeVisibility(visible = mapsEditViewModel.mapEditor?.mapRoles != null) {
                    val onClick = {
                        onNavigateRequest(Destination.MAPS_ROLES)
                    }
                    ExpressiveButtonRow(
                        title = stringResource(R.string.mapsRoles),
                        description = stringResource(R.string.mapsRoles_description)
                            .replace("{COUNT}", (mapsEditViewModel.mapEditor?.mapRoles?.size ?: 0).toString()),
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
                FadeVisibility(visible = mapsEditViewModel.mapEditor?.downloadableMaterials?.isNotEmpty() == true) {
                    val onClick = {
                        onNavigateRequest(Destination.MAPS_MATERIALS)
                    }
                    ExpressiveButtonRow(
                        title = stringResource(R.string.mapsMaterials),
                        description = stringResource(R.string.mapsMaterials_description)
                            .replace("%n", (mapsEditViewModel.mapEditor?.downloadableMaterials?.size ?: 0).toString()),
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
    mapsEditViewModel: MapsEditViewModel = koinViewModel()
) {
    val optionsComponents: List<@Composable () -> Unit> = mapsEditViewModel.mapEditor?.mapOptions.orEmpty().map { option -> {
        when (option.type) {
            LACMapOptionType.NUMBER -> TextField(
                value = option.value,
                onValueChange = {
                    option.value = it
                    mapsEditViewModel.mapEditor?.pushMapOptionsState()
                },
                label = {
                    Text(option.label)
                },
                isError = option.value.toIntOrNull() == null,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    autoCorrectEnabled = null
                ),
                modifier = Modifier.fillMaxWidth()
            )
            LACMapOptionType.BOOLEAN -> ExpressiveSwitchRow(
                title = option.label,
                checked = option.value == "true",
                onCheckedChange = {
                    option.value = it.toString()
                    mapsEditViewModel.mapEditor?.pushMapOptionsState()
                }
            )
            LACMapOptionType.SWITCH -> ExpressiveSwitchRow(
                title = option.label,
                checked = option.value == "enabled",
                onCheckedChange = {
                    option.value = if (it) "enabled" else "disabled"
                    mapsEditViewModel.mapEditor?.pushMapOptionsState()
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
    mapsEditViewModel: MapsEditViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val debugBadge: @Composable () -> Unit = {
        Text("DEBUG")
    }
    val debugButtons: List<@Composable () -> Unit> = if (mapsEditViewModel.prefs.debug.value) listOf(
        {
            ExpressiveButtonRow(
                title = "View getCurrentContent()",
                description = "without applyChanges()",
                trailingComponent = debugBadge
            ) {
                scope.launch(Dispatchers.IO) {
                    FileUtil.openTextAsFile(
                        text = mapsEditViewModel.mapEditor?.editor?.getCurrentContent()
                            ?: "content was null",
                        context = context
                    )
                }
            }
        },
        {
            ExpressiveButtonRow(
                title = "applyChanges() and view result",
                trailingComponent = debugBadge
            ) {
                scope.launch(Dispatchers.IO) {
                    FileUtil.openTextAsFile(
                        text = mapsEditViewModel.mapEditor?.editor?.applyChanges()
                            ?: "content was null",
                        context = context
                    )
                }
            }
        }
    ) else listOf()

    ExpressiveSection(stringResource(R.string.mapsEdit_misc)) {
        VerticalSegmentor(
            {
                FadeVisibility(visible = mapsEditViewModel.mapEditor?.replaceableObjects?.isEmpty() != true) {
                    ExpressiveButtonRow(
                        title = stringResource(R.string.mapsEdit_misc_replaceOldObjects),
                        description = stringResource(R.string.mapsEdit_misc_replaceOldObjects_description),
                        icon = {
                            ExpressiveRowIcon(
                                painter = rememberVectorPainter(Icons.Rounded.FindReplace)
                            )
                        }
                    ) {
                        mapsEditViewModel.replaceOldObjects(context)
                    }
                }
            },
            {
                val expandedRowColor = getExpandableRowDefaultExpandedContainerColor(fraction = 0.1f)
                ExpandableRow(
                    expanded = mapsEditViewModel.objectFilterExpanded,
                    title = stringResource(R.string.mapsEdit_filterObjects),
                    description = stringResource(R.string.mapsEdit_filterObjects_description),
                    icon = {
                        ExpressiveRowIcon(
                            painter = rememberVectorPainter(Icons.Rounded.FilterAlt)
                        )
                    },
                    expandedContainerColor = expandedRowColor,
                    onClickHeader = {
                        mapsEditViewModel.objectFilterExpanded = !mapsEditViewModel.objectFilterExpanded
                    }
                ) {
                    Column(Modifier.padding(vertical = 8.dp)) {
                        FilterObjects(containerColor = expandedRowColor)
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
                selected = mapsEditViewModel.objectFilter == suggestion,
                onClick = { mapsEditViewModel.objectFilter = suggestion },
                label = { Text(suggestion.filterName ?: "-") }
            )
        }
    }

    FilterChip(
        selected = mapsEditViewModel.objectFilter.caseSensitive,
        onClick = {
            mapsEditViewModel.objectFilter = mapsEditViewModel.objectFilter.copy(
                caseSensitive = !mapsEditViewModel.objectFilter.caseSensitive
            )
        },
        label = {
            Text(stringResource(R.string.mapsEdit_filterObjects_caseSensitive))
        },
        leadingIcon = if (mapsEditViewModel.objectFilter.caseSensitive) { {
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
        selectedIndex = if (mapsEditViewModel.objectFilter.exactMatch) 1 else 0,
        onSelect = {
            mapsEditViewModel.objectFilter = mapsEditViewModel.objectFilter.copy(
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
            onClick = { mapsEditViewModel.removeObjectFilterMatches(context) },
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