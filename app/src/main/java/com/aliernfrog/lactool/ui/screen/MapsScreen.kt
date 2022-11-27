package com.aliernfrog.lactool.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.state.MapsState
import com.aliernfrog.lactool.ui.composable.LACToolButton
import com.aliernfrog.lactool.ui.composable.LACToolColumnRounded
import com.aliernfrog.lactool.ui.composable.LACToolTextField
import com.aliernfrog.lactool.util.FileUtil
import kotlinx.coroutines.launch

@Composable
fun MapsScreen(mapsState: MapsState, navController: NavController) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { mapsState.getMapsFile(context); mapsState.getImportedMaps(); mapsState.getExportedMaps() }
    Column(Modifier.fillMaxSize().verticalScroll(mapsState.scrollState)) {
        PickMapFileButton(mapsState)
        MapActions(mapsState, navController)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PickMapFileButton(mapsState: MapsState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LACToolButton(
        title = context.getString(R.string.manageMapsPickMap),
        painter = painterResource(id = R.drawable.map),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        scope.launch { mapsState.pickMapSheetState.show() }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MapActions(mapsState: MapsState, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mapChosen = mapsState.chosenMap.value != null
    val isImported = mapsState.chosenMap.value?.filePath?.startsWith(mapsState.mapsDir) ?: false
    val isExported = mapsState.chosenMap.value?.filePath?.startsWith(mapsState.mapsExportDir) ?: false
    MapActionVisibility(visible = mapChosen) {
        LACToolColumnRounded(title = context.getString(R.string.manageMapsMapName)) {
            LACToolTextField(
                value = mapsState.mapNameEdit.value,
                placeholder = { Text(mapsState.chosenMap.value!!.mapName) },
                onValueChange = { mapsState.mapNameEdit.value = it },
                singleLine = true
            )
            MapActionVisibility(visible = isImported && mapsState.getMapNameEdit(false) != mapsState.chosenMap.value!!.mapName) {
                LACToolButton(
                    title = context.getString(R.string.manageMapsRename),
                    painter = painterResource(id = R.drawable.edit),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    scope.launch { mapsState.renameChosenMap(context) }
                }
            }
        }
    }
    MapActionVisibility(visible = mapChosen && !isImported) {
        LACToolButton(
            title = context.getString(R.string.manageMapsImport),
            painter = painterResource(id = R.drawable.download),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            scope.launch { mapsState.importChosenMap(context) }
        }
    }
    MapActionVisibility(visible = mapChosen && isImported) {
        LACToolButton(
            title = context.getString(R.string.manageMapsExport),
            painter = painterResource(id = R.drawable.share)
        ) {
            scope.launch { mapsState.exportChosenMap(context) }
        }
    }
    MapActionVisibility(visible = mapChosen) {
        LACToolButton(
            title = context.getString(R.string.manageMapsShare),
            painter = painterResource(id = R.drawable.share)
        ) {
            FileUtil.shareFile(mapsState.chosenMap.value!!.filePath, "text/plain", context)
        }
    }
    MapActionVisibility(visible = mapChosen) {
        LACToolButton(
            title = context.getString(R.string.manageMapsEdit),
            description = context.getString(R.string.manageMapsEditDescription),
            painter = painterResource(id = R.drawable.edit)
        ) {
            scope.launch { mapsState.editChosenMap(context, navController) }
        }
    }
    MapActionVisibility(visible = mapChosen && (isImported || isExported)) {
        LACToolButton(
            title = context.getString(R.string.manageMapsDelete),
            painter = painterResource(id = R.drawable.trash),
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ) {
            scope.launch { mapsState.deleteMapSheetState.show() }
        }
    }
}

@Composable
private fun MapActionVisibility(visible: Boolean, content: @Composable AnimatedVisibilityScope.() -> Unit) {
    return AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        content = content
    )
}