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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.AppTopBar
import com.aliernfrog.lactool.ui.component.ImageButton
import com.aliernfrog.lactool.ui.component.ImageButtonOverlay
import com.aliernfrog.lactool.ui.component.VerticalProgressIndicatorWithText
import com.aliernfrog.lactool.ui.dialog.MaterialsNoConnectionDialog
import com.aliernfrog.lactool.ui.viewmodel.MapsEditViewModel
import io.github.aliernfrog.pftool_shared.enum.ListStyle
import io.github.aliernfrog.pftool_shared.ui.component.FadeVisibility
import io.github.aliernfrog.pftool_shared.ui.component.FadeVisibilityColumn
import io.github.aliernfrog.pftool_shared.ui.component.ImageButtonInfo
import io.github.aliernfrog.pftool_shared.ui.component.LazyAdaptiveVerticalGrid
import io.github.aliernfrog.pftool_shared.ui.component.SEGMENTOR_SMALL_ROUNDNESS
import io.github.aliernfrog.pftool_shared.ui.component.VerticalSegmentor
import io.github.aliernfrog.pftool_shared.ui.component.expressive.ExpressiveButtonRow
import io.github.aliernfrog.pftool_shared.ui.component.expressive.ExpressiveRowIcon
import io.github.aliernfrog.pftool_shared.ui.component.form.ExpandableRow
import io.github.aliernfrog.pftool_shared.ui.component.verticalSegmentedShape
import io.github.aliernfrog.pftool_shared.ui.sheet.ListViewOptionsSheet
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MapsMaterialsScreen(
    mapsEditViewModel: MapsEditViewModel = koinViewModel(),
    onNavigateBackRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listStylePref = mapsEditViewModel.prefs.mapsMaterialsListOptions.styleGroup.getCurrent()
    val gridMaxLineSpanPref = mapsEditViewModel.prefs.mapsMaterialsListOptions.gridMaxLineSpanGroup.getCurrent()
    val listStyle = ListStyle.entries[listStylePref.value]

    LaunchedEffect(Unit) {
        if (!mapsEditViewModel.materialsLoaded)
            mapsEditViewModel.loadDownloadableMaterials(context)
    }

    LaunchedEffect(mapsEditViewModel.mapEditor?.downloadableMaterials?.size) {
        // Back out if materials list is empty, which means there's nothing much to do in this screen
        if (mapsEditViewModel.mapEditor?.downloadableMaterials.isNullOrEmpty())
            onNavigateBackRequest()
    }

    ListViewOptionsSheet(
        sheetState = mapsEditViewModel.materialsListOptionsSheetState,
        sorting = null,
        onSortingChange = null,
        sortingReversed = null,
        onSortingReversedChange = null,
        style = listStyle,
        onStyleChange = { listStylePref.value = it.ordinal },
        gridMaxLineSpan = gridMaxLineSpanPref.value,
        onGridMaxLineSpanChange = { gridMaxLineSpanPref.value = it }
    )

    AppScaffold(
        topBar = { scrollBehavior ->
            AppTopBar(
                title = stringResource(R.string.mapsMaterials),
                scrollBehavior = scrollBehavior,
                onNavigationClick = onNavigateBackRequest,
                actions = {
                    Box {
                        IconButton(
                            shapes = IconButtonDefaults.shapes(),
                            onClick = { scope.launch {
                                mapsEditViewModel.materialsListOptionsSheetState.show()
                            } }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = stringResource(R.string.list_options)
                            )
                        }
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
                    VerticalProgressIndicatorWithText(
                        progress = mapsEditViewModel.materialsLoadProgress
                    )
                }
            } else {
                val materials = mapsEditViewModel.mapEditor?.downloadableMaterials ?: listOf()
                AnimatedContent(targetState = listStyle) { style ->
                    when (style) {
                        ListStyle.LIST -> LazyColumn(Modifier.fillMaxSize()) {
                            item {
                                Suggestions(
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                            itemsIndexed(materials) { index, material ->
                                MaterialButton(
                                    material = material,
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .verticalSegmentedShape(index, totalSize = materials.size)
                                )
                            }
                            item {
                                Spacer(Modifier.navigationBarsPadding())
                            }
                        }
                        ListStyle.GRID -> LazyAdaptiveVerticalGrid(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            maxLineSpan = gridMaxLineSpanPref.value
                        ) { maxLineSpan: Int ->
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Suggestions(
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                            items(materials) {
                                MaterialButton(
                                    material = it,
                                    contentScale = ContentScale.Crop,
                                    minified = true,
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(SEGMENTOR_SMALL_ROUNDNESS))
                                )
                            }
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Spacer(Modifier.navigationBarsPadding())
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
    mapsEditViewModel: MapsEditViewModel = koinViewModel(),
    modifier: Modifier
) {
    val unusedMaterials = mapsEditViewModel.mapEditor?.downloadableMaterials?.filter { it.usedBy.isEmpty() } ?: listOf()
    val hasSuggestions = mapsEditViewModel.failedMaterials.isNotEmpty() || unusedMaterials.isNotEmpty()
    FadeVisibilityColumn(
        visible = hasSuggestions,
        modifier = modifier
    ) {
        VerticalSegmentor(
            {
                Suggestion(
                    title = stringResource(R.string.mapsMaterials_failedMaterials),
                    description = stringResource(R.string.mapsMaterials_failedMaterials_description),
                    painter = rememberVectorPainter(Icons.Default.Error),
                    accentColor = MaterialTheme.colorScheme.error,
                    materials = mapsEditViewModel.failedMaterials,
                    onMaterialClick = {
                        mapsEditViewModel.openDownloadableMaterialOptions(it)
                    }
                )
            },
            {
                Suggestion(
                    title = stringResource(R.string.mapsMaterials_unusedMaterials),
                    description = stringResource(R.string.mapsMaterials_unusedMaterials_description),
                    painter = rememberVectorPainter(Icons.Rounded.TipsAndUpdates),
                    accentColor = MaterialTheme.colorScheme.secondary,
                    materials = unusedMaterials,
                    onMaterialClick = {
                        mapsEditViewModel.openDownloadableMaterialOptions(it)
                    }
                )
            },
            dynamic = true,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun Suggestion(
    title: String,
    description: String,
    painter: Painter,
    accentColor: Color,
    materials: List<LACMapDownloadableMaterial>,
    onMaterialClick: (LACMapDownloadableMaterial) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    CompositionLocalProvider(LocalContentColor provides accentColor) {
        FadeVisibility(materials.isNotEmpty()) {
            ExpandableRow(
                expanded = expanded,
                title = title,
                description = description.replace("%n", materials.size.toString()),
                icon = {
                    ExpressiveRowIcon(painter = painter)
                },
                minimizedContainerColor = accentColor,
                onClickHeader = { expanded = !expanded }
            ) {
                Column {
                    materials.forEach { material ->
                        ExpressiveButtonRow(
                            title = material.name,
                            description = stringResource(R.string.mapsMaterials_clickToViewMore)
                        ) {
                            onMaterialClick(material)
                        }
                    }
                }
            }
        }
    }
}