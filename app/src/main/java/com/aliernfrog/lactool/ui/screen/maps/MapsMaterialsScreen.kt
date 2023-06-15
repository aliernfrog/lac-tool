package com.aliernfrog.lactool.ui.screen.maps

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.laclib.data.LACMapDownloadableMaterial
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.*
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.component.form.FormSection
import com.aliernfrog.lactool.ui.dialog.MaterialsNoConnectionDialog
import com.aliernfrog.lactool.ui.viewmodel.MapsEditViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsMaterialsScreen(
    mapsEditViewModel: MapsEditViewModel = getViewModel(),
    onNavigateBackRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    AppScaffold(
        title = stringResource(R.string.mapsMaterials),
        topAppBarState = mapsEditViewModel.materialsTopAppBarState,
        onBackClick = {
            onNavigateBackRequest()
        }
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
    MaterialsNoConnectionDialog()
}

@Composable
private fun Suggestions(
    mapsEditViewModel: MapsEditViewModel = getViewModel()
) {
    val scope = rememberCoroutineScope()
    val unusedMaterials = mapsEditViewModel.mapEditor?.downloadableMaterials?.filter { it.usedBy.isEmpty() } ?: listOf()
    val hasSuggestions = mapsEditViewModel.failedMaterials.isNotEmpty() || unusedMaterials.isNotEmpty()
    FadeVisibility(hasSuggestions) {
        FormSection(
            title = stringResource(R.string.mapsMaterials_suggestions),
            innerModifier = Modifier.padding(horizontal = 8.dp)
        ) {
            AnimatedVisibility(visible = mapsEditViewModel.failedMaterials.isNotEmpty()) {
                FailedMaterials(
                    failedMaterials = mapsEditViewModel.failedMaterials,
                    onMaterialClick = {
                        scope.launch { mapsEditViewModel.showMaterialSheet(it) }
                    }
                )
            }
            AnimatedVisibility(visible = unusedMaterials.isNotEmpty()) {
                UnusedMaterials(
                    unusedMaterials = unusedMaterials,
                    onScrollUpRequest = {
                        scope.launch { mapsEditViewModel.materialsLazyListState.scrollToItem(0) }
                    },
                    onMaterialClick = {
                        scope.launch { mapsEditViewModel.showMaterialSheet(it) }
                    }
                )
            }
        }
    }
}

@Composable
private fun FailedMaterials(
    failedMaterials: List<LACMapDownloadableMaterial>,
    onMaterialClick: (LACMapDownloadableMaterial) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExpandableColumnRounded(
        title = stringResource(R.string.mapsMaterials_failedMaterials),
        description = stringResource(R.string.mapsMaterials_failedMaterials_description)
            .replace("%n", failedMaterials.size.toString()),
        painter = rememberVectorPainter(Icons.Rounded.Report),
        headerContainerColor = MaterialTheme.colorScheme.error,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        failedMaterials.forEach { material ->
            ButtonRow(
                title = material.name,
                description = stringResource(R.string.mapsMaterials_clickToViewMore)
            ) {
                onMaterialClick(material)
            }
        }
    }
}

@Composable
private fun UnusedMaterials(
    unusedMaterials: List<LACMapDownloadableMaterial>,
    onScrollUpRequest: () -> Unit,
    onMaterialClick: (LACMapDownloadableMaterial) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExpandableColumnRounded(
        title = stringResource(R.string.mapsMaterials_unusedMaterials),
        description = stringResource(R.string.mapsMaterials_unusedMaterials_description)
            .replace("%n", unusedMaterials.size.toString()),
        painter = rememberVectorPainter(Icons.Rounded.TipsAndUpdates),
        headerContainerColor = MaterialTheme.colorScheme.secondary,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        unusedMaterials.forEach { material ->
            ButtonRow(
                title = material.name,
                description = stringResource(R.string.mapsMaterials_clickToViewMore)
            ) {
                onMaterialClick(material)
            }
        }
    }

    LaunchedEffect(Unit) {
        onScrollUpRequest()
    }
}