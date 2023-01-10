package com.aliernfrog.lactool.state

import android.content.Context
import androidx.compose.foundation.ScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LACMap
import com.aliernfrog.lactool.data.LACMapToMerge
import com.aliernfrog.lactool.util.extension.swap
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.lactool.util.staticutil.LACUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterialApi::class)
class MapsMergeState(
    _topToastState: TopToastState,
    _mapsState: MapsState
) {
    private val topToastState = _topToastState
    private val mapsState = _mapsState
    val pickMapSheetState = ModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scrollState = ScrollState(0)

    val chosenMaps = mutableStateListOf<LACMapToMerge>()
    val optionsExpandedFor = mutableStateOf(0)
    var mergeMapDialogShown by mutableStateOf(false)
    var isMerging by mutableStateOf(false)

    fun addMap(file: Any) {
        when (file) {
            is DocumentFileCompat -> {
                val mapName = FileUtil.removeExtension(file.name)
                chosenMaps.add(LACMapToMerge(
                    map = LACMap(name = mapName, fileName = file.name, documentFile = file)
                ))
            }
            is File -> {
                val mapName = file.nameWithoutExtension
                chosenMaps.add(LACMapToMerge(
                    map = LACMap(name = mapName, fileName = file.name, file = file)
                ))
            }
            else -> throw IllegalArgumentException()
        }
    }

    fun makeMapBase(index: Int, mapName: String, context: Context) {
        chosenMaps.swap(0, index)
        optionsExpandedFor.value = 0
        topToastState.showToast(
            text = context.getString(R.string.mapsMerge_map_madeBase).replace("%MAP%", mapName),
            icon = Icons.Rounded.Done
        )
    }

    fun removeMap(index: Int, mapName: String, context: Context) {
        chosenMaps.removeAt(index)
        optionsExpandedFor.value = 0
        topToastState.showToast(
            text = context.getString(R.string.mapsMerge_map_removed).replace("%MAP%", mapName),
            icon = Icons.Rounded.Done
        )
    }

    suspend fun mergeMaps(mapName: String, navController: NavController, context: Context) {
        if (chosenMaps.size < 2) return cancelMerging(R.string.mapsMerge_noEnoughMaps)
        val mapsFile = mapsState.getMapsFile(context)
        val newFileName = "$mapName.txt"
        val output = mapsFile.findFile(newFileName)
        if (output != null && output.exists()) return cancelMerging(R.string.maps_alreadyExists)
        isMerging = true
        withContext(Dispatchers.IO) {
            val newMapLines = mutableListOf<String>()
            Thread.sleep(2000) // Can't live without seeing progress indicator
            chosenMaps.forEachIndexed { index, mapToMerge ->
                val isBaseMap = index == 0
                val filtered = LACUtil.filterMapToMergeContent(mapToMerge, isBaseMap, context)
                newMapLines.addAll(filtered)
            }
            val newFile = mapsFile.createFile("", newFileName)
            val outputStream = context.contentResolver.openOutputStream(newFile!!.uri)!!
            val outputStreamWriter = outputStream.writer(Charsets.UTF_8)
            outputStreamWriter.write(newMapLines.joinToString("\n"))
            outputStreamWriter.flush()
            outputStreamWriter.close()
            outputStream.close()
            mergeMapDialogShown = false
            isMerging = false
            chosenMaps.clear()
            mapsState.getMap(documentFile = newFile)
            topToastState.showToast(context.getString(R.string.mapsMerge_merged).replace("%MAP%", mapName), icon = Icons.Rounded.Done)
        }
        navController.popBackStack()
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