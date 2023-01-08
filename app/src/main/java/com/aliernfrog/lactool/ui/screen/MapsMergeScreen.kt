package com.aliernfrog.lactool.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LACMap
import com.aliernfrog.lactool.data.LACMapToMerge
import com.aliernfrog.lactool.state.MapsMergeState
import com.aliernfrog.lactool.ui.component.ButtonRounded
import com.aliernfrog.lactool.ui.component.ColumnRounded
import com.aliernfrog.lactool.ui.component.MapToMerge
import com.aliernfrog.lactool.ui.dialog.MergeMapDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MapsMergeScreen(mapsMergeState: MapsMergeState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Box {
        Column(Modifier.fillMaxSize().verticalScroll(mapsMergeState.scrollState)) {
            PickMapButton(mapsMergeState)
            MapsList(mapsMergeState)
            Spacer(Modifier.height(70.dp))
        }
        AnimatedVisibility(
            visible = mapsMergeState.chosenMaps.size >= 2,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            ExtendedFloatingActionButton(
                onClick = { mapsMergeState.mergeMapDialogShown = true },
                modifier = Modifier.padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Rounded.Build,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(stringResource(R.string.mapsMerge_merge))
            }
        }
    }
    if (mapsMergeState.mergeMapDialogShown) MergeMapDialog(
        isMerging = mapsMergeState.isMerging,
        onDismissRequest = { mapsMergeState.mergeMapDialogShown = false },
        onConfirm = { scope.launch { mapsMergeState.mergeMaps(it, context) } }
    )
}

@Composable
private fun MapsList(mapsMergeState: MapsMergeState) {
    val baseMap = mapsMergeState.chosenMaps.firstOrNull()
    val mapsToMerge = mapsMergeState.chosenMaps.toList().drop(1)
    AnimatedVisibility(baseMap != null) {
        MapButtonWithActions(
            mapsMergeState = mapsMergeState,
            mapToMerge = baseMap ?: LACMapToMerge(LACMap("-", "-")),
            mapIndex = 0
        )
    }
    AnimatedVisibility(mapsToMerge.isNotEmpty()) {
        ColumnRounded(
            title = stringResource(R.string.mapsMerge_mapsToMerge)
        ) {
            mapsToMerge.forEachIndexed { index, map ->
                MapButtonWithActions(
                    mapsMergeState = mapsMergeState,
                    mapToMerge = map,
                    mapIndex = index+1,
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PickMapButton(mapsMergeState: MapsMergeState) {
    val scope = rememberCoroutineScope()
    ButtonRounded(
        title = stringResource(R.string.mapsMerge_addMap),
        painter = rememberVectorPainter(Icons.Rounded.AddLocationAlt),
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        scope.launch { mapsMergeState.pickMapSheetState.show() }
    }
}

@Composable
private fun MapButtonWithActions(
    mapsMergeState: MapsMergeState,
    mapToMerge: LACMapToMerge,
    mapIndex: Int,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val context = LocalContext.current
    val isBase = mapIndex == 0
    val expanded = mapsMergeState.optionsExpandedFor.value == mapIndex
    MapToMerge(
        mapToMerge = mapToMerge,
        isBaseMap = isBase,
        expanded = expanded || isBase,
        showExpandedIndicator = !isBase,
        containerColor = containerColor,
        onMakeBase = { mapsMergeState.makeMapBase(mapIndex, mapToMerge.map.name, context) },
        onRemove = { mapsMergeState.removeMap(mapIndex, mapToMerge.map.name, context) },
        onClick = {
            mapsMergeState.optionsExpandedFor.value = if (expanded) 0
            else mapIndex
        }
    )
}