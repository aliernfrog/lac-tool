package com.aliernfrog.lactool.state

import android.content.Context
import androidx.compose.foundation.ScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.aliernfrog.laclib.map.LACMapMerger
import com.aliernfrog.laclib.util.MAP_MERGER_MIN_REQUIRED_MAPS
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
class MapsMergeState(
    _topToastState: TopToastState,
    _mapsState: MapsState
) {
    private val topToastState = _topToastState
    private val mapsState = _mapsState
    val pickMapSheetState = ModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val topAppBarState = TopAppBarState(0F, 0F, 0F)
    val scrollState = ScrollState(0)

    var mapMerger by mutableStateOf(LACMapMerger(), neverEqualPolicy())
    val optionsExpandedFor = mutableStateOf(0)
    var mergeMapDialogShown by mutableStateOf(false)
    var isMerging by mutableStateOf(false)

    suspend fun addMap(file: Any, context: Context) {
        withContext(Dispatchers.IO) {
            val mapName = when (file) {
                is DocumentFileCompat -> FileUtil.removeExtension(file.name)
                is File -> file.nameWithoutExtension
                else -> throw IllegalArgumentException()
            }
            val inputStream = when (file) {
                is DocumentFileCompat -> context.contentResolver.openInputStream(file.uri)
                is File -> file.inputStream()
                else -> throw IllegalArgumentException()
            } ?: return@withContext
            val content = inputStream.bufferedReader().readText()
            inputStream.close()
            mapMerger.addMap(mapName, content)
            updateMergerState()
        }
    }

    fun makeMapBase(index: Int, mapName: String, context: Context) {
        mapMerger.makeMapBase(index)
        optionsExpandedFor.value = 0
        topToastState.showToast(
            text = context.getString(R.string.mapsMerge_map_madeBase).replace("{MAP}", mapName),
            icon = Icons.Rounded.Done
        )
        updateMergerState()
    }

    fun removeMap(index: Int, mapName: String, context: Context) {
        mapMerger.mapsToMerge.removeAt(index)
        optionsExpandedFor.value = 0
        topToastState.showToast(
            text = context.getString(R.string.mapsMerge_map_removed).replace("{MAP}", mapName),
            icon = Icons.Rounded.Done
        )
        updateMergerState()
    }

    fun hasEnoughMaps(): Boolean {
        return mapMerger.mapsToMerge.size >= MAP_MERGER_MIN_REQUIRED_MAPS
    }

    fun updateMergerState() {
        mapMerger = mapMerger
    }

    suspend fun mergeMaps(mapName: String, navController: NavController, context: Context) {
        if (!hasEnoughMaps()) return cancelMerging(R.string.mapsMerge_noEnoughMaps)
        val mapsFile = mapsState.getMapsFile(context)
        val newFileName = "$mapName.txt"
        val output = mapsFile.findFile(newFileName)
        if (output != null && output.exists()) return cancelMerging(R.string.maps_alreadyExists)
        isMerging = true
        withContext(Dispatchers.IO) {
            Thread.sleep(2000) // Can't live without seeing progress indicator
            val newMapContent = mapMerger.mergeMaps(
                onNoEnoughMaps = { cancelMerging(R.string.mapsMerge_noEnoughMaps) }
            ) ?: return@withContext
            val newFile = mapsFile.createFile("", newFileName)
            val outputStream = context.contentResolver.openOutputStream(newFile!!.uri)!!
            val outputStreamWriter = outputStream.writer(Charsets.UTF_8)
            outputStreamWriter.write(newMapContent)
            outputStreamWriter.flush()
            outputStreamWriter.close()
            outputStream.close()
            mergeMapDialogShown = false
            isMerging = false
            mapMerger.mapsToMerge.clear()
            // No need to update merger state here because it navigates back to maps screen after finishing
            mapsState.getMap(documentFile = newFile)
            topToastState.showToast(context.getString(R.string.mapsMerge_merged).replace("{MAP}", mapName), icon = Icons.Rounded.Done)
        }
        navController.popBackStack()
    }

    suspend fun loadMaps(context: Context) {
        mapsState.getMapsFile(context)
        mapsState.getImportedMaps()
        mapsState.getExportedMaps()
    }

    /**
     * Cancels merging and hides merge map dialog
     * @param reason Text to show in toast
     */
    private fun cancelMerging(reason: Any) {
        topToastState.showToast(reason, Icons.Rounded.PriorityHigh, TopToastColor.ERROR)
        mergeMapDialogShown = false
        isMerging = false
    }
}