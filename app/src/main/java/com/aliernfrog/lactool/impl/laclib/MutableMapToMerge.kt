package com.aliernfrog.lactool.impl.laclib

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aliernfrog.laclib.data.LACMapToMerge

class MutableMapToMerge(mapToMerge: LACMapToMerge) {
    val mapName = mapToMerge.mapName
    val content = mapToMerge.content

    var mergePosition by mutableStateOf(mapToMerge.mergePosition)
    var mergeSpawnpoints by mutableStateOf(mapToMerge.mergeSpawnpoints)
    var mergeRacingCheckpoints by mutableStateOf(mapToMerge.mergeRacingCheckpoints)
    var mergeTDMSpawnpoints by mutableStateOf(mapToMerge.mergeTDMSpawnpoints)

    fun toImmutable() = LACMapToMerge(
        mapName = mapName,
        content = content,
        mergePosition = mergePosition,
        mergeSpawnpoints = mergeSpawnpoints,
        mergeRacingCheckpoints = mergeRacingCheckpoints,
        mergeTDMSpawnpoints = mergeTDMSpawnpoints
    )
}