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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LACMap
import com.aliernfrog.lactool.state.MapsMergeState
import com.aliernfrog.lactool.ui.component.ButtonRounded
import com.aliernfrog.lactool.ui.component.ColumnRounded
import com.aliernfrog.lactool.ui.component.MapButton
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
            expandable = { Text("TODO finish this", Modifier.padding(8.dp)) }
        ) {}
    }
    AnimatedVisibility(mapsToMerge.isNotEmpty()) {
        ColumnRounded(
            title = stringResource(R.string.mapsMerge_mapsToMerge)
        ) {
            mapsToMerge.forEach { map ->
                var expanded by remember { mutableStateOf(false) }
                MapButton(
                    map = map,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    expanded = expanded,
                    expandable = {
                        Text(
                            text = "TODO finish this",
                            color = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                ) {
                    expanded = !expanded
                    //TODO
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