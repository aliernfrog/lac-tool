package com.aliernfrog.lactool.ui.screen.maps

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.laclib.data.LACMapDownloadableMaterial
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.enum.ListStyle
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.AppTopBar
import com.aliernfrog.lactool.ui.component.FadeVisibility
import com.aliernfrog.lactool.ui.component.FadeVisibilityColumn
import com.aliernfrog.lactool.ui.component.ImageButton
import com.aliernfrog.lactool.ui.component.ImageButtonInfo
import com.aliernfrog.lactool.ui.component.ImageButtonOverlay
import com.aliernfrog.lactool.ui.component.LazyAdaptiveVerticalGrid
import com.aliernfrog.lactool.ui.component.ListViewOptionsDropdown
import com.aliernfrog.lactool.ui.component.VerticalProgressIndicator
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.component.form.DividerRow
import com.aliernfrog.lactool.ui.component.form.ExpandableRow
import com.aliernfrog.lactool.ui.dialog.MaterialsNoConnectionDialog
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.ui.viewmodel.MapsEditViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsMaterialsScreen(
    mapsEditViewModel: MapsEditViewModel = koinViewModel(),
    onNavigateBackRequest: () -> Unit
) {
    val context = LocalContext.current
    val listStyle = ListStyle.entries[mapsEditViewModel.prefs.mapsMaterialsListStyle.value]
    var listOptionsExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!mapsEditViewModel.materialsLoaded)
            mapsEditViewModel.loadDownloadableMaterials(context)
    }

    LaunchedEffect(mapsEditViewModel.mapEditor?.downloadableMaterials?.size) {
        // Back out if materials list is empty, which means there's nothing much to do in this screen
        if (mapsEditViewModel.mapEditor?.downloadableMaterials.isNullOrEmpty())
            onNavigateBackRequest()
    }

    AppScaffold(
        topBar = { scrollBehavior ->
            AppTopBar(
                title = stringResource(R.string.mapsMaterials),
                scrollBehavior = scrollBehavior,
                onNavigationClick = onNavigateBackRequest,
                actions = {
                    Box {
                        IconButton(
                            onClick = { listOptionsExpanded = true }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = stringResource(R.string.list_options)
                            )
                        }
                        ListViewOptionsDropdown(
                            expanded = listOptionsExpanded,
                            onDismissRequest = { listOptionsExpanded = false },
                            sortingPref = null,
                            sortingReversedPref = null,
                            stylePref = mapsEditViewModel.prefs.mapsMaterialsListStyle
                        )
                    }
                }
            )
        }
    ) {
        @Composable
        fun MaterialButton(
            material: LACMapDownloadableMaterial,
            contentScale: ContentScale = ContentScale.FillWidth,
            minified: Boolean = false,
            modifier: Modifier = Modifier
        ) {
            val failed = mapsEditViewModel.failedMaterials.contains(material)
            ImageButton(
                model = material.url,
                contentScale = contentScale,
                containerColor = if (failed) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.surfaceContainerHigh,
                onClick = {
                    mapsEditViewModel.openDownloadableMaterialOptions(material)
                },
                modifier = modifier
                    .padding(8.dp)
                    .clip(AppComponentShape)
            ) {
                if (!minified || failed || material.usedBy.isEmpty()) ImageButtonOverlay(
                    title = if (minified) null else material.name,
                    modifier = Modifier.align(Alignment.BottomStart),
                    containerColor = if (failed) MaterialTheme.colorScheme.error
                    else if (material.usedBy.isEmpty()) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceContainer
                ) {
                    if (minified) return@ImageButtonOverlay Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (failed) Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = stringResource(R.string.mapsMaterials_failed)
                        )
                        if (material.usedBy.isEmpty()) Icon(
                            imageVector = Icons.Default.TipsAndUpdates,
                            contentDescription = stringResource(R.string.mapsMaterials_unused)
                        )
                    }
                    if (material.usedBy.isNotEmpty()) ImageButtonInfo(
                        text = stringResource(R.string.mapsMaterials_usedCount)
                            .replace("%n", material.usedBy.size.toString()),
                        icon = rememberVectorPainter(Icons.Default.ViewInAr)
                    ) else ImageButtonInfo(
                        text = stringResource(R.string.mapsMaterials_unused),
                        icon = rememberVectorPainter(Icons.Default.TipsAndUpdates)
                    )
                    if (failed) ImageButtonInfo(
                        text = stringResource(R.string.mapsMaterials_failed),
                        icon = rememberVectorPainter(Icons.Default.Error)
                    )
                }
            }
        }

        Crossfade(targetState = mapsEditViewModel.materialsLoaded) { loaded ->
            if (!loaded) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    VerticalProgressIndicator(
                        progress = mapsEditViewModel.materialsLoadProgress
                    )
                }
            } else {
                val materials = mapsEditViewModel.mapEditor?.downloadableMaterials ?: listOf()
                AnimatedContent(targetState = listStyle) { style ->
                    when (style) {
                        ListStyle.LIST -> LazyColumn(Modifier.fillMaxSize()) {
                            item {
                                Suggestions()
                            }
                            items(materials) {
                                MaterialButton(it)
                            }
                            item {
                                Spacer(Modifier.systemBarsPadding())
                            }
                        }
                        ListStyle.GRID -> LazyAdaptiveVerticalGrid(
                            modifier = Modifier.fillMaxSize()
                        ) { maxLineSpan: Int ->
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Suggestions()
                            }
                            items(materials) {
                                MaterialButton(
                                    material = it,
                                    contentScale = ContentScale.Crop,
                                    minified = true,
                                    modifier = Modifier.aspectRatio(1f)
                                )
                            }
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Spacer(Modifier.systemBarsPadding())
                            }
                        }
                    }
                }
            }
        }
    }

    MaterialsNoConnectionDialog()
}

@Composable
private fun Suggestions(
    mapsEditViewModel: MapsEditViewModel = koinViewModel()
) {
    val unusedMaterials = mapsEditViewModel.mapEditor?.downloadableMaterials?.filter { it.usedBy.isEmpty() } ?: listOf()
    val hasSuggestions = mapsEditViewModel.failedMaterials.isNotEmpty() || unusedMaterials.isNotEmpty()
    FadeVisibilityColumn(hasSuggestions) {
        Suggestion(
            titleRef = R.string.mapsMaterials_failedMaterials,
            descriptionRef = R.string.mapsMaterials_failedMaterials_description,
            painter = rememberVectorPainter(Icons.Default.Error),
            accentColor = MaterialTheme.colorScheme.error,
            materials = mapsEditViewModel.failedMaterials,
            onMaterialClick = {
                mapsEditViewModel.openDownloadableMaterialOptions(it)
            }
        )
        Suggestion(
            titleRef = R.string.mapsMaterials_unusedMaterials,
            descriptionRef = R.string.mapsMaterials_unusedMaterials_description,
            painter = rememberVectorPainter(Icons.Rounded.TipsAndUpdates),
            accentColor = MaterialTheme.colorScheme.secondary,
            materials = unusedMaterials,
            onMaterialClick = {
                mapsEditViewModel.openDownloadableMaterialOptions(it)
            }
        )
        DividerRow(Modifier.padding(8.dp))
    }
}

@Composable
private fun Suggestion(
    titleRef: Int,
    descriptionRef: Int,
    painter: Painter,
    accentColor: Color,
    materials: List<LACMapDownloadableMaterial>,
    onMaterialClick: (LACMapDownloadableMaterial) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    FadeVisibility(materials.isNotEmpty()) {
        ExpandableRow(
            expanded = expanded,
            title = stringResource(titleRef),
            description = stringResource(descriptionRef).replace("%n", materials.size.toString()),
            painter = painter,
            minimizedHeaderContentColor = accentColor,
            expandedHeaderColor = accentColor,
            onClickHeader = { expanded = !expanded }
        ) {
            materials.forEach { material ->
                ButtonRow(
                    title = material.name,
                    description = stringResource(R.string.mapsMaterials_clickToViewMore)
                ) {
                    onMaterialClick(material)
                }
            }
        }
    }
}