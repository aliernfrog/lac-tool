package com.aliernfrog.lactool.ui.sheet

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LACMap
import com.aliernfrog.lactool.enum.PickMapSheetSegments
import com.aliernfrog.lactool.ui.component.AppModalBottomSheet
import com.aliernfrog.lactool.ui.component.ButtonRounded
import com.aliernfrog.lactool.ui.component.ErrorWithIcon
import com.aliernfrog.lactool.ui.component.MapButton
import com.aliernfrog.lactool.ui.component.SegmentedButtons
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
    onFilePick: (file: Any) -> Boolean
) {
    val scope = rememberCoroutineScope()
    var mapThumbnailsShown by remember { mutableStateOf(getShowMapThumbnails()) }

    fun pickFile(file: Any) {
        if (onFilePick(file)) scope.launch {
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
                pickFile(it)
            }
        )
        Maps(
            importedMaps = mapsViewModel.importedMaps,
            exportedMaps = mapsViewModel.exportedMaps,
            showMapThumbnails = mapThumbnailsShown,
            onFilePick = {
                pickFile(it)
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
    ButtonRounded(
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
    onFilePick: (Any) -> Unit
) {
    var selectedSegment by remember { mutableIntStateOf(PickMapSheetSegments.IMPORTED.ordinal) }
    SegmentedButtons(
        options = listOf(
            stringResource (R.string.maps_pickMap_imported),
            stringResource(R.string.maps_pickMap_exported)
        )
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
                onFilePick = onFilePick
            )
        }
    }
}

@Composable
private fun MapsList(
    maps: List<LACMap>,
    isShowingExportedMaps: Boolean,
    showMapThumbnails: Boolean,
    onFilePick: (Any) -> Unit
) {
    if (maps.isNotEmpty()) {
        maps.forEach { map ->
            MapButton(map, showMapThumbnail = showMapThumbnails) {
                val file = map.documentFile ?: map.file
                if (file != null) onFilePick(file)
            }
        }
    } else {
        ErrorWithIcon(
            error = stringResource(if (isShowingExportedMaps) R.string.maps_pickMap_noExportedMaps else R.string.maps_pickMap_noImportedMaps),
            painter = rememberVectorPainter(Icons.Rounded.LocationOff)
        )
    }
}