package com.aliernfrog.lactool.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LACMap
import com.aliernfrog.lactool.data.LACMapToMerge
import com.aliernfrog.lactool.state.MapsMergeState
import com.aliernfrog.lactool.ui.component.ButtonRounded
import com.aliernfrog.lactool.ui.component.ColumnRounded
import com.aliernfrog.lactool.ui.component.MapToMerge
import kotlinx.coroutines.launch

@Composable
fun MapsMergeScreen(mapsMergeState: MapsMergeState) {
    Column(Modifier.fillMaxSize().verticalScroll(mapsMergeState.scrollState)) {
        PickMapButton(mapsMergeState)
        MapsList(mapsMergeState)
    }
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