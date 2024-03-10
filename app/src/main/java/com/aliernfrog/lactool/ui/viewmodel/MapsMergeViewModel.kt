package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import androidx.compose.foundation.ScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.aliernfrog.laclib.map.LACMapMerger
import com.aliernfrog.laclib.util.MAP_MERGER_MIN_REQUIRED_MAPS
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.util.extension.popBackStackSafe
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
class MapsMergeViewModel(
    private val topToastState: TopToastState,
    private val mainViewModel: MainViewModel,
    private val mapsViewModel: MapsViewModel
) : ViewModel() {
    val navController: NavController
        get() = mainViewModel.navController
    
    val topAppBarState = TopAppBarState(0F, 0F, 0F)
    val scrollState = ScrollState(0)

    var isMerging by mutableStateOf(false)
    var mapMerger by mutableStateOf(LACMapMerger(), neverEqualPolicy())
    var optionsExpandedFor by mutableIntStateOf(0)
    var mergeMapDialogShown by mutableStateOf(false)
    var mapListShown by mutableStateOf(false)

    val hasEnoughMaps get() = mapMerger.mapsToMerge.size >= MAP_MERGER_MIN_REQUIRED_MAPS

    suspend fun mergeMaps(
        context: Context,
        newMapName: String
    ) {
        if (!hasEnoughMaps) return cancelMerging(R.string.mapsMerge_noEnoughMaps)
        val mapsFile = mapsViewModel.getMapsFile(context)
        val newFileName = "$newMapName.txt"
        val output = mapsFile.findFile(newFileName)
        if (output != null && output.exists()) return cancelMerging(R.string.maps_alreadyExists)
        isMerging = true
        withContext(Dispatchers.IO) {
            Thread.sleep(2000) // Can't live without seeing progress indicator
            val newMapContent = mapMerger.mergeMaps(
                onNoEnoughMaps = { cancelMerging(R.string.mapsMerge_noEnoughMaps) }
            ) ?: return@withContext
            mapsFile.createFile(newFileName)!!.writeFile(newMapContent, context)
            mergeMapDialogShown = false
            isMerging = false
            mapMerger.mapsToMerge.clear()
            // No need to update merger state here because it navigates back after finishing
            mapsViewModel.chooseMap(mapsFile.findFile(newFileName))
            topToastState.showToast(context.getString(R.string.mapsMerge_merged).replace("{MAP}", newMapName), icon = Icons.Rounded.Done)
        }
        navController.popBackStackSafe()
    }

    suspend fun addMaps(
        context: Context,
        vararg maps: MapFile
    ) {
        withContext(Dispatchers.IO) {
            maps.forEach { map ->
                val inputStream =  map.file.inputStream(context)!!
                val content = inputStream.bufferedReader().readText()
                inputStream.close()
                mapMerger.addMap(map.name, content)
            }
            updateMergerState()
        }
    }

    fun removeMap(index: Int, mapName: String, context: Context) {
        mapMerger.mapsToMerge.removeAt(index)
        optionsExpandedFor = 0
        topToastState.showToast(
            text = context.getString(R.string.mapsMerge_map_removed).replace("{MAP}", mapName),
            icon = Icons.Rounded.Done
        )
        updateMergerState()
    }


    fun makeMapBase(index: Int, mapName: String, context: Context) {
        mapMerger.makeMapBase(index)
        optionsExpandedFor = 0
        topToastState.showToast(
            text = context.getString(R.string.mapsMerge_map_madeBase).replace("{MAP}", mapName),
            icon = Icons.Rounded.Done
        )
        updateMergerState()
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

    fun updateMergerState() {
        mapMerger = mapMerger
    }
}