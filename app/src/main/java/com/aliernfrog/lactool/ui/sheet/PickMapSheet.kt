package com.aliernfrog.lactool.ui.sheet

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LACMap
import com.aliernfrog.lactool.enum.PickMapSheetSegments
import com.aliernfrog.lactool.ui.component.AppModalBottomSheet
import com.aliernfrog.lactool.ui.component.ErrorWithIcon
import com.aliernfrog.lactool.ui.component.MapButton
import com.aliernfrog.lactool.ui.component.SegmentedButtons
import com.aliernfrog.lactool.ui.component.form.RoundedButtonRow
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel
import com.aliernfrog.lactool.util.staticutil.UriToFileUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.enum.TopToastType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.getViewModel
import java.io.File

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PickMapSheet(
    mapsViewModel: MapsViewModel = getViewModel(),
    sheetState: ModalBottomSheetState = mapsViewModel.pickMapSheetState,
    getShowMapThumbnails: () -> Boolean = { mapsViewModel.prefs.showMapThumbnailsInList },
    onMapPick: (map: Any) -> Boolean
) {
    val scope = rememberCoroutineScope()
    var mapThumbnailsShown by remember { mutableStateOf(getShowMapThumbnails()) }

    fun pickMap(map: Any) {
        if (onMapPick(map)) scope.launch {
            sheetState.hide()
        }
    }

    AppModalBottomSheet(
        title = stringResource(R.string.maps_pickMap),
        sheetState = sheetState
    ) {
        PickFromDeviceButton(
            onPathConversionFail = {
                mapsViewModel.topToastState.showToast(
                    text = R.string.warning_couldntConvertToPath,
                    icon = Icons.Rounded.PriorityHigh,
                    iconTintColor = TopToastColor.ERROR,
                    type = TopToastType.ANDROID
                )
            },
            onFilePick = {
                pickMap(it)
            }
        )
        Maps(
            importedMaps = mapsViewModel.importedMaps,
            exportedMaps = mapsViewModel.exportedMaps,
            showMapThumbnails = mapThumbnailsShown,
            onMapPick = {
                pickMap(it)
            }
        )
    }

    LaunchedEffect(sheetState.isVisible) {
        if (sheetState.isVisible) mapThumbnailsShown = getShowMapThumbnails()
    }
}

@Composable
private fun PickFromDeviceButton(
    onPathConversionFail: () -> Unit,
    onFilePick: (File) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.data?.data != null) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    val convertedPath = UriToFileUtil.getRealFilePath(it.data?.data!!, context)
                    if (convertedPath != null) onFilePick(File(convertedPath))
                    else onPathConversionFail()
                }
            }
        }
    }
    RoundedButtonRow(
        title = stringResource(R.string.maps_pickMap_device),
        painter = rememberVectorPainter(Icons.Rounded.Folder),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).setType("text/plain")
        launcher.launch(intent)
    }
}

@Composable
private fun Maps(
    importedMaps: List<LACMap>,
    exportedMaps: List<LACMap>,
    showMapThumbnails: Boolean,
    onMapPick: (Any) -> Unit
) {
    var selectedSegment by remember { mutableIntStateOf(PickMapSheetSegments.IMPORTED.ordinal) }
    SegmentedButtons(
        options = listOf(
            stringResource(R.string.maps_pickMap_imported),
            stringResource(R.string.maps_pickMap_exported)
        ),
        selectedOptionIndex = selectedSegment,
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        selectedSegment = it
    }
    AnimatedContent(targetState = selectedSegment) {
        Column {
            val maps = if (it == PickMapSheetSegments.IMPORTED.ordinal) importedMaps else exportedMaps
            MapsList(
                maps = maps,
                isShowingExportedMaps = it == PickMapSheetSegments.EXPORTED.ordinal,
                showMapThumbnails = showMapThumbnails,
                onMapPick = onMapPick
            )
        }
    }
}

@Composable
private fun MapsList(
    maps: List<LACMap>,
    isShowingExportedMaps: Boolean,
    showMapThumbnails: Boolean,
    onMapPick: (Any) -> Unit
) {
    if (maps.isNotEmpty()) {
        maps.forEach { map ->
            MapButton(map, showMapThumbnail = showMapThumbnails) {
                onMapPick(map)
            }
        }
    } else {
        ErrorWithIcon(
            error = stringResource(if (isShowingExportedMaps) R.string.maps_pickMap_noExportedMaps else R.string.maps_pickMap_noImportedMaps),
            painter = rememberVectorPainter(Icons.Rounded.LocationOff)
        )
    }
}