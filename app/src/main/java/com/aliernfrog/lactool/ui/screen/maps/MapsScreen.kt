package com.aliernfrog.lactool.ui.screen.maps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.FadeVisibility
import com.aliernfrog.lactool.ui.component.FadeVisibilityColumn
import com.aliernfrog.lactool.ui.component.TextField
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.component.form.RoundedButtonRow
import com.aliernfrog.lactool.ui.dialog.DeleteConfirmationDialog
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.ui.theme.AppInnerComponentShape
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel
import com.aliernfrog.lactool.util.Destination
import com.aliernfrog.lactool.util.extension.resolveFile
import com.aliernfrog.lactool.util.staticutil.FileUtil
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MapsScreen(
    mapsViewModel: MapsViewModel = getViewModel(),
    onNavigateRequest: (Destination) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        mapsViewModel.getMapsFile(context)
        mapsViewModel.fetchAllMaps()
    }
    AppScaffold(
        title = stringResource(R.string.maps),
        topAppBarState = mapsViewModel.topAppBarState
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(mapsViewModel.scrollState)) {
            PickMapFileButton {
                scope.launch { mapsViewModel.pickMapSheetState.show() }
            }
            MapActions(
                mapsViewModel = mapsViewModel,
                onNavigateRequest = onNavigateRequest
            )
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).alpha(0.7f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            OtherActions(
                onNavigateMapsMergeScreenRequest = {
                    onNavigateRequest(Destination.MAPS_MERGE)
                }
            )
        }
    }
    if (mapsViewModel.mapDeleteDialogShown) DeleteConfirmationDialog(
        name = mapsViewModel.lastMapName,
        onDismissRequest = { mapsViewModel.mapDeleteDialogShown = false },
        onConfirmDelete = {
            scope.launch {
                mapsViewModel.deleteChosenMap()
                mapsViewModel.mapDeleteDialogShown = false
            }
        }
    )
}

@Composable
private fun PickMapFileButton(
    onClick: () -> Unit
) {
    RoundedButtonRow(
        title = stringResource(R.string.maps_pickMap),
        painter = rememberVectorPainter(Icons.Rounded.LocationOn),
        containerColor = MaterialTheme.colorScheme.primary,
        onClick = onClick
    )
}

@Composable
private fun MapActions(
    mapsViewModel: MapsViewModel,
    onNavigateRequest: (Destination) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mapChosen = mapsViewModel.chosenMap != null
    val isImported = mapsViewModel.getChosenMapPath()?.startsWith(mapsViewModel.mapsDir) ?: false
    val isExported = mapsViewModel.getChosenMapPath()?.startsWith(mapsViewModel.exportedMapsDir) ?: false
    val mapNameUpdated = mapsViewModel.getMapNameEdit(false) != mapsViewModel.chosenMap?.name
    FadeVisibilityColumn(visible = mapChosen) {
        TextField(
            value = mapsViewModel.mapNameEdit,
            onValueChange = { mapsViewModel.mapNameEdit = it },
            label = { Text(stringResource(R.string.maps_mapName)) },
            placeholder = { Text(mapsViewModel.chosenMap!!.name) },
            leadingIcon = rememberVectorPainter(Icons.Rounded.TextFields),
            singleLine = true,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            doneIcon = rememberVectorPainter(Icons.Rounded.Edit),
            doneIconShown = isImported && mapNameUpdated,
            onDone = {
                scope.launch { mapsViewModel.renameChosenMap() }
            }
        )
        Divider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).alpha(0.7f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        )
    }
    FadeVisibility(mapChosen) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clip(AppComponentShape),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FadeVisibility(!isImported) {
                ButtonRow(
                    title = stringResource(R.string.maps_import),
                    painter = rememberVectorPainter(Icons.Rounded.Download),
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = AppInnerComponentShape
                ) {
                    scope.launch { mapsViewModel.importChosenMap(context) }
                }
            }
            FadeVisibility(isImported) {
                ButtonRow(
                    title = stringResource(R.string.maps_export),
                    painter = rememberVectorPainter(Icons.Rounded.Upload),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    shape = AppInnerComponentShape
                ) {
                    scope.launch { mapsViewModel.exportChosenMap(context) }
                }
            }
            ButtonRow(
                title = stringResource(R.string.maps_share),
                painter = rememberVectorPainter(Icons.Rounded.IosShare),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = AppInnerComponentShape
            ) {
                scope.launch { FileUtil.shareFile(mapsViewModel.chosenMap!!.resolveFile(), context) }
            }
            ButtonRow(
                title = stringResource(R.string.maps_edit),
                description = stringResource(R.string.maps_edit_description),
                painter = rememberVectorPainter(Icons.Rounded.Edit),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = AppInnerComponentShape
            ) {
                scope.launch {
                    mapsViewModel.editChosenMap(
                        context = context,
                        onNavigateMapEditScreenRequest = {
                            onNavigateRequest(Destination.MAPS_EDIT)
                        }
                    )
                }
            }
            FadeVisibility(visible = mapChosen && (isImported || isExported)) {
                ButtonRow(
                    title = stringResource(R.string.maps_delete),
                    painter = rememberVectorPainter(Icons.Rounded.Delete),
                    containerColor = MaterialTheme.colorScheme.error,
                    shape = AppInnerComponentShape
                ) {
                    mapsViewModel.mapDeleteDialogShown = true
                }
            }
        }
    }
}

@Composable
private fun OtherActions(onNavigateMapsMergeScreenRequest: () -> Unit) {
    RoundedButtonRow(
        title = stringResource(R.string.mapsMerge),
        painter = rememberVectorPainter(Icons.Rounded.AddLocationAlt)
    ) {
        onNavigateMapsMergeScreenRequest()
    }
}