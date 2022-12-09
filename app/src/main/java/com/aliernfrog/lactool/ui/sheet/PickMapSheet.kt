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
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.MapsListItem
import com.aliernfrog.lactool.enum.PickMapSheetSegments
import com.aliernfrog.lactool.state.MapsState
import com.aliernfrog.lactool.ui.composable.*
import com.aliernfrog.lactool.util.staticutil.UriToFileUtil
import com.aliernfrog.toptoast.TopToastColorType
import com.aliernfrog.toptoast.TopToastManager
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun PickMapSheet(
    mapsState: MapsState,
    topToastManager: TopToastManager,
    sheetState: ModalBottomSheetState,
    scrollState: ScrollState = rememberScrollState(),
    showMapThumbnails: Boolean = true,
    onFilePick: (File) -> Unit,
    onDocumentFilePick: (DocumentFileCompat) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val hideSheet = { scope.launch { sheetState.hide() } }
    LACToolModalBottomSheet(title = stringResource(R.string.manageMapsPickMap), sheetState, scrollState) {
        PickFromDeviceButton(topToastManager) { onFilePick(it); hideSheet() }
        Maps(mapsState, showMapThumbnails, { onFilePick(it); hideSheet() }, { onDocumentFilePick(it); hideSheet() })
    }
    LaunchedEffect(sheetState.isVisible) {
        if (sheetState.isVisible) keyboardController?.hide()
        else scrollState.scrollTo(0)
    }
}

@Composable
private fun PickFromDeviceButton(topToastManager: TopToastManager, onFilePick: (File) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.data?.data != null) {
            val convertedPath = UriToFileUtil.getRealFilePath(it.data?.data!!, context)
            if (convertedPath != null) onFilePick(File(convertedPath))
            else topToastManager.showToast(context.getString(R.string.warning_couldntConvertToPath), iconImageVector = Icons.Rounded.PriorityHigh, iconTintColorType = TopToastColorType.ERROR)
        }
    }
    LACToolButtonRounded(title = stringResource(R.string.manageMapsPickMapFromDevice), painter = rememberVectorPainter(Icons.Rounded.Folder), containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).setType("text/plain").putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        launcher.launch(intent)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Maps(mapsState: MapsState, showMapThumbnails: Boolean, onFilePick: (File) -> Unit, onDocumentFilePick: (DocumentFileCompat) -> Unit) {
    var selectedSegment by remember { mutableStateOf(PickMapSheetSegments.IMPORTED.ordinal) }
    LACToolSegmentedButtons(options = listOf(stringResource (R.string.manageMapsPickMapYourMaps), stringResource(R.string.manageMapsPickMapExportedMaps))) {
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
            LACToolMapButton(map, showMapThumbnail = showMapThumbnails) {
                if (map.documentFile != null) onDocumentFilePick(map.documentFile)
                else if (map.file != null) onFilePick(map.file)
            }
        }
    } else {
        LACToolColumnRounded(color = MaterialTheme.colorScheme.error) {
            Text(text = stringResource(if (exportedMaps) R.string.manageMapsPickMapNoExportedMaps else R.string.manageMapsPickMapNoImportedMaps), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onError)
        }
    }
}