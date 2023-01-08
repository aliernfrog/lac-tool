package com.aliernfrog.lactool.state

import android.content.Context
import androidx.compose.foundation.ScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LACMap
import com.aliernfrog.lactool.data.LACMapToMerge
import com.aliernfrog.lactool.util.extension.swap
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import java.io.File

@OptIn(ExperimentalMaterialApi::class)
class MapsMergeState(
    _topToastState: TopToastState
) {
    private val topToastState = _topToastState
    val pickMapSheetState = ModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scrollState = ScrollState(0)

    val chosenMaps = mutableStateListOf<LACMapToMerge>()
    val optionsExpandedFor = mutableStateOf(0)

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
}