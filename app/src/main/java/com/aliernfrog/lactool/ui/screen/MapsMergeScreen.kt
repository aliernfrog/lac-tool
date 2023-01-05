package com.aliernfrog.lactool.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LACMap
import com.aliernfrog.lactool.state.MapsMergeState
import com.aliernfrog.lactool.ui.component.ButtonRounded
import com.aliernfrog.lactool.ui.component.ColumnRounded
import com.aliernfrog.lactool.ui.component.MapButton
import com.aliernfrog.lactool.util.extension.swap
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
    val baseMap = mapsMergeState.chosenMaps.firstOrNull() ?: LACMap("-", "-")
    val mapsToMerge = mapsMergeState.chosenMaps.toList().drop(1)
    AnimatedVisibility(baseMap.fileName != "-") {
        MapButton(
            map = baseMap,
            expanded = true,
            expandable = {
                Text(
                    text = stringResource(R.string.mapsMerge_base_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                MapActions(
                    map = baseMap,
                    mapsMergeState = mapsMergeState,
                    isBase = true,
                    containersColor = MaterialTheme.colorScheme.secondary
                )
            }
        ) {}
    }
    AnimatedVisibility(mapsToMerge.isNotEmpty()) {
        ColumnRounded(
            title = stringResource(R.string.mapsMerge_mapsToMerge)
        ) {
            mapsToMerge.forEach { map ->
                val expanded = mapsMergeState.optionsExpandedFor.value == map
                MapButton(
                    map = map,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    expanded = expanded,
                    expandable = {
                        MapActions(map, mapsMergeState, isBase = false)
                    }
                ) {
                    mapsMergeState.optionsExpandedFor.value = if (expanded) null
                    else map
                }
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
private fun MapActions(
    map: LACMap,
    mapsMergeState: MapsMergeState,
    isBase: Boolean,
    containersColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    if (!isBase) ButtonRounded(
        title = stringResource(R.string.mapsMerge_map_makeBase),
        containerColor = containersColor
    ) {
        mapsMergeState.chosenMaps.swap(0, mapsMergeState.chosenMaps.indexOf(map))
    }
    ButtonRounded(
        title = stringResource(R.string.mapsMerge_map_remove),
        containerColor = containersColor
    ) {
        mapsMergeState.chosenMaps.remove(map)
    }
}