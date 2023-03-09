package com.aliernfrog.lactool.ui.screen

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
import androidx.navigation.NavController
import com.aliernfrog.laclib.data.LACMapDownloadableMaterial
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.state.MapsEditState
import com.aliernfrog.lactool.ui.component.*
import com.aliernfrog.lactool.ui.dialog.MaterialsNoConnectionDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsMaterialsScreen(mapsEditState: MapsEditState, navController: NavController) {
    val scope = rememberCoroutineScope()
    AppScaffold(
        title = stringResource(R.string.mapsMaterials),
        topAppBarState = mapsEditState.materialsTopAppBarState,
        onBackClick = {
            navController.popBackStack()
        }
    ) {
        val materials = mapsEditState.mapEditor?.downloadableMaterials ?: listOf()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = mapsEditState.materialsLazyListState
        ) {
            item {
                Suggestions(mapsEditState)
            }
            items(materials) {
                val failed = mapsEditState.failedMaterials.contains(it)
                ImageButton(
                    model = it.url,
                    title = it.name,
                    description = stringResource(if (failed) R.string.mapsMaterials_failed else R.string.mapsMaterials_usedCount).replace("%n", it.usedBy.size.toString()),
                    painter = if (failed) rememberVectorPainter(Icons.Rounded.Report) else null,
                    containerColor = if (failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                    onError = { _ ->
                        scope.launch { mapsEditState.onDownloadableMaterialError(it) }
                    }
                ) {
                    scope.launch { mapsEditState.showMaterialSheet(it) }
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
private fun Suggestions(mapsEditState: MapsEditState) {
    val unusedMaterials = mapsEditState.mapEditor?.downloadableMaterials?.filter { it.usedBy.isEmpty() } ?: listOf()
    val hasSuggestions = mapsEditState.failedMaterials.isNotEmpty() || unusedMaterials.isNotEmpty()
    AnimatedVisibility(
        visible = hasSuggestions,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        ColumnDivider(
            title = stringResource(R.string.mapsMaterials_suggestions),
            innerModifier = Modifier.padding(horizontal = 8.dp)
        ) {
            AnimatedVisibility(visible = mapsEditState.failedMaterials.isNotEmpty()) {
                FailedMaterials(mapsEditState)
            }
            AnimatedVisibility(visible = unusedMaterials.isNotEmpty()) {
                UnusedMaterials(unusedMaterials, mapsEditState)
            }
        }
    }
}

@Composable
private fun FailedMaterials(mapsEditState: MapsEditState) {
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    ExpandableColumnRounded(
        title = stringResource(R.string.mapsMaterials_failedMaterials),
        description = stringResource(R.string.mapsMaterials_failedMaterials_description).replace("%n", mapsEditState.failedMaterials.size.toString()),
        painter = rememberVectorPainter(Icons.Rounded.Report),
        headerContainerColor = MaterialTheme.colorScheme.error,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        mapsEditState.failedMaterials.forEach { material ->
            ButtonShapeless(
                title = material.name,
                description = stringResource(R.string.mapsMaterials_clickToViewMore)
            ) {
                scope.launch {
                    mapsEditState.showMaterialSheet(material)
                }
            }
        }
    }
}

@Composable
private fun UnusedMaterials(unusedMaterials: List<LACMapDownloadableMaterial>, mapsEditState: MapsEditState) {
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    ExpandableColumnRounded(
        title = stringResource(R.string.mapsMaterials_unusedMaterials),
        description = stringResource(R.string.mapsMaterials_unusedMaterials_description).replace("%n", unusedMaterials.size.toString()),
        painter = rememberVectorPainter(Icons.Rounded.TipsAndUpdates),
        headerContainerColor = MaterialTheme.colorScheme.secondary,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        unusedMaterials.forEach { material ->
            ButtonShapeless(
                title = material.name,
                description = stringResource(R.string.mapsMaterials_clickToViewMore)
            ) {
                scope.launch {
                    mapsEditState.showMaterialSheet(material)
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        if (unusedMaterials.isNotEmpty()) mapsEditState.materialsLazyListState.scrollToItem(0)
    }
}