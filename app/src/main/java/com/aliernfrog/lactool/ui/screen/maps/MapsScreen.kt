package com.aliernfrog.lactool.ui.screen.maps

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.enum.MapImportedState
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.FadeVisibility
import com.aliernfrog.lactool.ui.component.TextField
import com.aliernfrog.lactool.ui.component.VerticalSegmentedButtons
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.component.maps.PickMapButton
import com.aliernfrog.lactool.ui.dialog.DeleteConfirmationDialog
import com.aliernfrog.lactool.ui.viewmodel.MapsMergeViewModel
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel
import com.aliernfrog.lactool.util.Destination
import com.aliernfrog.lactool.util.extension.resolveFile
import com.aliernfrog.lactool.util.staticutil.FileUtil
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen(
    mapsViewModel: MapsViewModel = getViewModel(),
    onNavigateRequest: (Destination) -> Unit
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(mapsViewModel.chosenMap) {
        if (mapsViewModel.chosenMap == null) mapsViewModel.mapListShown = true
    }

    BackHandler(mapsViewModel.chosenMap != null) {
        mapsViewModel.chooseMap(null)
    }

    AppScaffold(
        title = stringResource(R.string.maps),
        topAppBarState = mapsViewModel.topAppBarState
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(mapsViewModel.scrollState)) {
            PickMapButton(
                chosenMap = mapsViewModel.chosenMap,
                showMapThumbnail = mapsViewModel.prefs.showChosenMapThumbnail
            ) {
                mapsViewModel.mapListShown = true
            }
            MapActions(
                mapsViewModel = mapsViewModel,
                onNavigateRequest = onNavigateRequest
            )
        }
    }

    mapsViewModel.pendingMapDelete?.let {
        DeleteConfirmationDialog(
            name = it,
            onDismissRequest = { mapsViewModel.pendingMapDelete = null },
            onConfirmDelete = {
                scope.launch {
                    mapsViewModel.deleteChosenMap()
                    mapsViewModel.pendingMapDelete = null
                }
            }
        )
    }
}

@Composable
private fun MapActions(
    mapsMergeViewModel: MapsMergeViewModel = getViewModel(),
    mapsViewModel: MapsViewModel,
    onNavigateRequest: (Destination) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isImported = mapsViewModel.chosenMap?.importedState == MapImportedState.IMPORTED
    val isExported = mapsViewModel.chosenMap?.importedState == MapImportedState.EXPORTED
    val mapNameUpdated = mapsViewModel.resolveMapNameInput(false) != mapsViewModel.chosenMap?.name
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
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).alpha(0.7f),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.surfaceVariant
    )
    VerticalSegmentedButtons(
        {
            FadeVisibility(!isImported) {
                ButtonRow(
                    title = stringResource(R.string.maps_import),
                    painter = rememberVectorPainter(Icons.Rounded.Download),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    scope.launch { mapsViewModel.importChosenMap(context) }
                }
            }
        },
        {
            FadeVisibility(isImported) {
                ButtonRow(
                    title = stringResource(R.string.maps_export),
                    painter = rememberVectorPainter(Icons.Rounded.Upload),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    scope.launch { mapsViewModel.exportChosenMap(context) }
                }
            }
        },
        {
            ButtonRow(
                title = stringResource(R.string.maps_share),
                painter = rememberVectorPainter(Icons.Rounded.IosShare),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                scope.launch { FileUtil.shareFile(mapsViewModel.chosenMap!!.resolveFile(), context) }
            }
        },
        {
            ButtonRow(
                title = stringResource(R.string.maps_edit),
                description = stringResource(R.string.maps_edit_description),
                painter = rememberVectorPainter(Icons.Rounded.Edit),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
        },
        {
            ButtonRow(
                title = stringResource(R.string.maps_merge),
                description = stringResource(R.string.maps_merge_description),
                painter = rememberVectorPainter(Icons.Rounded.AddLocationAlt),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                mapsViewModel.chosenMap?.let {
                    scope.launch {
                        mapsMergeViewModel.addMap(it, context)
                        onNavigateRequest(Destination.MAPS_MERGE)
                    }
                }
            }
        },
        {
            FadeVisibility(visible = isImported || isExported) {
                ButtonRow(
                    title = stringResource(R.string.maps_delete),
                    painter = rememberVectorPainter(Icons.Rounded.Delete),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.error
                ) {
                    mapsViewModel.pendingMapDelete = mapsViewModel.chosenMap?.name
                }
            }
        },
        modifier = Modifier.padding(8.dp)
    )
}
