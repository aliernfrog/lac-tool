package com.aliernfrog.lactool.impl.laclib

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aliernfrog.laclib.map.LACMapMerger

class MapMergerState(
    private val merger: LACMapMerger
) {
    var mapsToMerge by mutableStateOf(merger.mapsToMerge.map { MutableMapToMerge(it) })
        private set

    private fun pullMapsState() {
        mapsToMerge = merger.mapsToMerge.map { MutableMapToMerge(it) }
    }

    fun pushMapsState() {
        merger.mapsToMerge = mapsToMerge.map { it.toImmutable() }.toMutableList()
    }

    fun addMap(mapName: String, content: String) {
        merger.addMap(mapName, content)
        pullMapsState()
    }

    fun removeMap(index: Int) {
        merger.mapsToMerge.removeAt(index)
        pullMapsState()
    }

    fun clearMaps() {
        merger.mapsToMerge.clear()
        pullMapsState()
    }

    fun makeMapBase(index: Int) {
        merger.makeMapBase(index)
        pullMapsState()
    }

    fun mergeMaps(onNoEnoughMaps: () -> Unit) = merger.mergeMaps(
        onNoEnoughMaps = onNoEnoughMaps
    )
}