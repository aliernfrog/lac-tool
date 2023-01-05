package com.aliernfrog.lactool.state

import androidx.compose.foundation.ScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.aliernfrog.lactool.data.LACMap
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

    val chosenMaps = mutableStateListOf<LACMap>()
    val optionsExpandedFor = mutableStateOf<Int?>(null)

    fun addMap(file: Any) {
        when (file) {
            is DocumentFileCompat -> {
                val mapName = FileUtil.removeExtension(file.name)
                // TODO current method to get thumbnail uri from map uri is too genius, a less genius one can be used instead
                chosenMaps.add(LACMap(name = mapName, fileName = file.name, documentFile = file, thumbnailPainterModel = file.uri.toString().replace("txt", "jpg")))
            }
            is File -> {
                val mapName = file.nameWithoutExtension
                chosenMaps.add(LACMap(name = mapName, fileName = file.name, file = file))
            }
            else -> throw IllegalArgumentException()
        }
    }
}