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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.aliernfrog.lactool.PickMapSheetSegments
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.MapsListItem
import com.aliernfrog.lactool.state.MapsState
import com.aliernfrog.lactool.ui.composable.*
import com.aliernfrog.lactool.util.UriToFileUtil
import com.aliernfrog.toptoast.TopToastColorType
import com.aliernfrog.toptoast.TopToastManager
import com.lazygeniouz.filecompat.file.DocumentFileCompat
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
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val hideSheet = { scope.launch { sheetState.hide() } }
    LACToolModalBottomSheet(title = context.getString(R.string.manageMapsPickMap), sheetState, scrollState) {
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
            else topToastManager.showToast(context.getString(R.string.warning_couldntConvertToPath), iconDrawableId = R.drawable.exclamation, iconTintColorType = TopToastColorType.ERROR)
        }
    }
    LACToolButton(title = context.getString(R.string.manageMapsPickMapFromDevice), painter = painterResource(id = R.drawable.device), containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).setType("text/plain").putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        launcher.launch(intent)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Maps(mapsState: MapsState, showMapThumbnails: Boolean, onFilePick: (File) -> Unit, onDocumentFilePick: (DocumentFileCompat) -> Unit) {
    val context = LocalContext.current
    var selectedSegment by remember { mutableStateOf(PickMapSheetSegments.IMPORTED) }
    LACToolSegmentedButtons(options = listOf(context.getString(R.string.manageMapsPickMapYourMaps),context.getString(R.string.manageMapsPickMapExportedMaps))) {
        selectedSegment = it
    }
    AnimatedContent(targetState = selectedSegment) {
        Column {
            val maps = if (it == PickMapSheetSegments.IMPORTED) mapsState.importedMaps else mapsState.exportedMaps
            MapsList(maps = maps.value, showMapThumbnails, exportedMaps = it == PickMapSheetSegments.EXPORTED, onFilePick, onDocumentFilePick)
        }
    }
}

@Composable
private fun MapsList(maps: List<MapsListItem>, showMapThumbnails: Boolean, exportedMaps: Boolean, onFilePick: (File) -> Unit, onDocumentFilePick: (DocumentFileCompat) -> Unit) {
    val context = LocalContext.current
    if (maps.isNotEmpty()) {
        maps.forEach { map ->
            LACToolMapButton(map, showMapThumbnail = showMapThumbnails) {
                if (map.documentFile != null) onDocumentFilePick(map.documentFile)
                else if (map.file != null) onFilePick(map.file)
            }
        }
    } else {
        LACToolColumnRounded(color = MaterialTheme.colorScheme.error) {
            Text(text = context.getString(if (exportedMaps) R.string.manageMapsPickMapNoExportedMaps else R.string.manageMapsPickMapNoImportedMaps), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onError)
        }
    }
}