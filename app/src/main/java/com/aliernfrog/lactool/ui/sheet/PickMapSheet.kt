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
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.aliernfrog.lactool.ui.component.ButtonIcon
import com.aliernfrog.lactool.ui.component.ErrorWithIcon
import com.aliernfrog.lactool.ui.component.SegmentedButtons
import com.aliernfrog.lactool.ui.component.maps.MapButton
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel
import com.aliernfrog.lactool.util.staticutil.UriUtil
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
    selectedSegment: PickMapSheetSegments = mapsViewModel.pickMapSheetSelectedSegment,
    getShowMapThumbnails: () -> Boolean = { mapsViewModel.prefs.showMapThumbnailsInList },
    onSelectedSegmentChange: (PickMapSheetSegments) -> Unit = {
        mapsViewModel.pickMapSheetSelectedSegment = it
    },
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
            onFail = {
                mapsViewModel.topToastState.showToast(
                    text = R.string.maps_pickMap_failed,
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
            maps = selectedSegment.getMaps(mapsViewModel),
            showMapThumbnails = mapThumbnailsShown,
            selectedSegment = selectedSegment,
            onSelectedSegmentChange = onSelectedSegmentChange,
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
    onFail: () -> Unit,
    onFilePick: (File) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.data?.data != null) scope.launch {
            withContext(Dispatchers.IO) {
                val cachedFile = UriUtil.cacheFile(
                    uri = it.data?.data!!,
                    parentName = "maps",
                    context = context
                )
                if (cachedFile != null) onFilePick(cachedFile)
                else onFail()
            }
        }
    }
    Button(
        onClick = {
            val intent = Intent(Intent.ACTION_GET_CONTENT).setType("application/zip")
            launcher.launch(intent)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        ButtonIcon(rememberVectorPainter(Icons.Outlined.FolderZip))
        Text(stringResource(R.string.maps_pickMap_device))
    }
}

@Composable
private fun Maps(
    maps: List<LACMap>,
    showMapThumbnails: Boolean,
    selectedSegment: PickMapSheetSegments,
    onSelectedSegmentChange: (PickMapSheetSegments) -> Unit,
    onMapPick: (Any) -> Unit
) {
    SegmentedButtons(
        options = listOf(
            stringResource(R.string.maps_pickMap_imported),
            stringResource(R.string.maps_pickMap_exported)
        ),
        selectedOptionIndex = selectedSegment.ordinal,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        onSelectedSegmentChange(PickMapSheetSegments.values()[it])
    }
    AnimatedContent(targetState = selectedSegment) {
        Column {
            MapsList(
                maps = maps,
                noMapsTextId = it.noMapsId,
                showMapThumbnails = showMapThumbnails,
                onMapPick = onMapPick
            )
        }
    }
}

@Composable
private fun MapsList(
    maps: List<LACMap>,
    noMapsTextId: Int,
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
            error = stringResource(noMapsTextId),
            painter = rememberVectorPainter(Icons.Rounded.LocationOff)
        )
    }
}