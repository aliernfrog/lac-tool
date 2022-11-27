package com.aliernfrog.lactool.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LacMapType
import com.aliernfrog.lactool.state.MapsEditState
import com.aliernfrog.lactool.ui.composable.*
import kotlinx.coroutines.launch

@Composable
fun MapsEditScreen(mapsEditState: MapsEditState, navController: NavController) {
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize()) {
        Actions(mapsEditState)
        LACToolFAB(
            icon = Icons.Default.Done,
            modifier = Modifier.align(Alignment.BottomEnd),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            scope.launch { mapsEditState.finishEditing(navController) }
        }
    }
}

@Composable
private fun Actions(mapsEditState: MapsEditState) {
    val context = LocalContext.current
    val typesExpanded = remember { mutableStateOf(false) }
    Column(Modifier.animateContentSize().verticalScroll(mapsEditState.scrollState)) {
        AnimatedVisibility(visible = mapsEditState.serverName.value != null) {
            LACToolTextField(
                value = mapsEditState.serverName.value ?: "",
                onValueChange = { mapsEditState.serverName.value = it },
                label = { Text(context.getString(R.string.mapsEdit_serverName)) },
                singleLine = true,
                containerColor = MaterialTheme.colorScheme.surface,
                rounded = false
            )
        }
        AnimatedVisibility(visible = mapsEditState.mapType.value != null) {
            LACToolButtonShapeless(
                title = context.getString(R.string.mapsEdit_mapType),
                description = getMapTypes().find { it.index == mapsEditState.mapType.value }?.label ?: "unknown",
                expanded = typesExpanded.value
            ) {
                typesExpanded.value = !typesExpanded.value
            }
            AnimatedVisibility(
                visible = typesExpanded.value,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                LACToolColumnRounded {
                    LACToolRadioButtons(
                        options = getMapTypes().map { it.label },
                        initialIndex = mapsEditState.mapType.value ?: 0,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        optionsRounded = true,
                        onSelect = { mapsEditState.mapType.value = it }
                    )
                }
            }
        }
        Text(mapsEditState.mapLines?.joinToString("\n") ?: "", Modifier.padding(horizontal = 8.dp))
    }
}

@Composable
private fun getMapTypes(): List<LacMapType> {
    val context = LocalContext.current
    return listOf(
        LacMapType(0, context.getString(R.string.mapsEdit_mapType_0)),
        LacMapType(1, context.getString(R.string.mapsEdit_mapType_1)),
        LacMapType(2, context.getString(R.string.mapsEdit_mapType_2)),
        LacMapType(3, context.getString(R.string.mapsEdit_mapType_3)),
        LacMapType(4, context.getString(R.string.mapsEdit_mapType_4)),
        LacMapType(5, context.getString(R.string.mapsEdit_mapType_5))
    )
}