package com.aliernfrog.lactool.ui.screen.maps

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.laclib.data.LACMapDownloadableMaterial
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.AppTopBar
import com.aliernfrog.lactool.ui.component.FadeVisibility
import com.aliernfrog.lactool.ui.component.FadeVisibilityColumn
import com.aliernfrog.lactool.ui.component.ImageButton
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.component.form.DividerRow
import com.aliernfrog.lactool.ui.component.form.ExpandableRow
import com.aliernfrog.lactool.ui.dialog.DeleteConfirmationDialog
import com.aliernfrog.lactool.ui.dialog.MaterialsNoConnectionDialog
import com.aliernfrog.lactool.ui.sheet.DownloadableMaterialSheet
import com.aliernfrog.lactool.ui.viewmodel.MapsEditViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsMaterialsScreen(
    mapsEditViewModel: MapsEditViewModel = koinViewModel(),
    onNavigateBackRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                onNavigationClick = {
                    onNavigateBackRequest()
                }
            )
        },
        topAppBarState = mapsEditViewModel.materialsTopAppBarState
    ) {
        val materials = mapsEditViewModel.mapEditor?.downloadableMaterials ?: listOf()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = mapsEditViewModel.materialsLazyListState
        ) {
            item {
                Suggestions()
            }
            items(materials) {
                val failed = mapsEditViewModel.failedMaterials.contains(it)
                ImageButton(
                    model = it.url,
                    title = it.name,
                    description = stringResource(if (failed) R.string.mapsMaterials_failed else R.string.mapsMaterials_usedCount).replace("%n", it.usedBy.size.toString()),
                    painter = if (failed) rememberVectorPainter(Icons.Rounded.Report) else null,
                    containerColor = if (failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                    onError = { _ ->
                        scope.launch { mapsEditViewModel.onDownloadableMaterialError(it) }
                    }
                ) {
                    scope.launch { mapsEditViewModel.showMaterialSheet(it) }
                }
            }
            item {
                Spacer(Modifier.systemBarsPadding())
            }
        }
    }

    mapsEditViewModel.pendingMaterialDelete?.let {
        DeleteConfirmationDialog(
            name = it.name,
            onDismissRequest = { mapsEditViewModel.pendingMaterialDelete = null }
        ) {
            mapsEditViewModel.deleteDownloadableMaterial(it, context)
            mapsEditViewModel.pendingMaterialDelete = null
            scope.launch {
                mapsEditViewModel.materialSheetState.hide()
                mapsEditViewModel.materialSheetChosenMaterial = null
            }
        }
    }

    MaterialsNoConnectionDialog()

    DownloadableMaterialSheet(
        material = mapsEditViewModel.materialSheetChosenMaterial,
        failed = mapsEditViewModel.materialSheetMaterialFailed,
        state = mapsEditViewModel.materialSheetState,
        topToastState = mapsEditViewModel.topToastState,
        onDeleteRequest = {
            mapsEditViewModel.pendingMaterialDelete = it
        },
        onError = {
            mapsEditViewModel.materialSheetMaterialFailed = true
        }
    )
}

@Composable
private fun Suggestions(
    mapsEditViewModel: MapsEditViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val unusedMaterials = mapsEditViewModel.mapEditor?.downloadableMaterials?.filter { it.usedBy.isEmpty() } ?: listOf()
    val hasSuggestions = mapsEditViewModel.failedMaterials.isNotEmpty() || unusedMaterials.isNotEmpty()
    FadeVisibilityColumn(hasSuggestions) {
        Suggestion(
            titleRef = R.string.mapsMaterials_failedMaterials,
            descriptionRef = R.string.mapsMaterials_failedMaterials_description,
            painter = rememberVectorPainter(Icons.Rounded.Report),
            accentColor = MaterialTheme.colorScheme.error,
            materials = mapsEditViewModel.failedMaterials,
            onMaterialClick = {
                scope.launch { mapsEditViewModel.showMaterialSheet(it) }
            }
        )
        Suggestion(
            titleRef = R.string.mapsMaterials_unusedMaterials,
            descriptionRef = R.string.mapsMaterials_unusedMaterials_description,
            painter = rememberVectorPainter(Icons.Rounded.TipsAndUpdates),
            accentColor = MaterialTheme.colorScheme.secondary,
            materials = unusedMaterials,
            onMaterialClick = {
                scope.launch { mapsEditViewModel.showMaterialSheet(it) }
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