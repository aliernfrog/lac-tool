package com.aliernfrog.lactool.ui.sheet

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.MapsListItem
import com.aliernfrog.lactool.enum.PickMapSheetSegments
import com.aliernfrog.lactool.state.MapsState
import com.aliernfrog.lactool.ui.component.*
import com.aliernfrog.lactool.util.staticutil.UriToFileUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun PickMapSheet(
    mapsState: MapsState,
    topToastState: TopToastState,
    sheetState: ModalBottomSheetState,
    scrollState: ScrollState = rememberScrollState(),
    showMapThumbnails: Boolean = true,
    onFilePick: (File) -> Unit,
    onDocumentFilePick: (DocumentFileCompat) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val hideSheet = { scope.launch { sheetState.hide() } }
    ModalBottomSheet(title = stringResource(R.string.maps_pickMap), sheetState, scrollState) {
        PickFromDeviceButton(topToastState) { onFilePick(it); hideSheet() }
        Maps(mapsState, showMapThumbnails, { onFilePick(it); hideSheet() }, { onDocumentFilePick(it); hideSheet() })
    }
    LaunchedEffect(sheetState.isVisible) {
        if (sheetState.isVisible) keyboardController?.hide()
        else scrollState.scrollTo(0)
    }
}

@Composable
private fun PickFromDeviceButton(topToastState: TopToastState, onFilePick: (File) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.data?.data != null) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    val convertedPath = UriToFileUtil.getRealFilePath(it.data?.data!!, context)
                    if (convertedPath != null) onFilePick(File(convertedPath))
                    else topToastState.showToast(R.string.warning_couldntConvertToPath, Icons.Rounded.PriorityHigh, TopToastColor.ERROR)
                }
            }
        }
    }
    ButtonRounded(title = stringResource(R.string.maps_pickMap_device), painter = rememberVectorPainter(Icons.Rounded.Folder), containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).setType("text/plain")
        launcher.launch(intent)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Maps(mapsState: MapsState, showMapThumbnails: Boolean, onFilePick: (File) -> Unit, onDocumentFilePick: (DocumentFileCompat) -> Unit) {
    var selectedSegment by remember { mutableStateOf(PickMapSheetSegments.IMPORTED.ordinal) }
    SegmentedButtons(options = listOf(stringResource (R.string.maps_pickMap_imported), stringResource(R.string.maps_pickMap_exported))) {
        selectedSegment = it
    }
    AnimatedContent(targetState = selectedSegment) {
        Column {
            val maps = if (it == PickMapSheetSegments.IMPORTED.ordinal) mapsState.importedMaps else mapsState.exportedMaps
            MapsList(maps = maps.value, showMapThumbnails, exportedMaps = it == PickMapSheetSegments.EXPORTED.ordinal, onFilePick, onDocumentFilePick)
        }
    }
}

@Composable
private fun MapsList(maps: List<MapsListItem>, showMapThumbnails: Boolean, exportedMaps: Boolean, onFilePick: (File) -> Unit, onDocumentFilePick: (DocumentFileCompat) -> Unit) {
    if (maps.isNotEmpty()) {
        maps.forEach { map ->
            MapButton(map, showMapThumbnail = showMapThumbnails) {
                if (map.documentFile != null) onDocumentFilePick(map.documentFile)
                else if (map.file != null) onFilePick(map.file)
            }
        }
    } else {
        ErrorWithIcon(
            error = stringResource(if (exportedMaps) R.string.maps_pickMap_noExportedMaps else R.string.maps_pickMap_noImportedMaps),
            painter = rememberVectorPainter(Icons.Rounded.LocationOff)
        )
    }
}