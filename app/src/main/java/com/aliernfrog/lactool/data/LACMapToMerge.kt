package com.aliernfrog.lactool.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class LACMapToMerge(
    val map: LACMap,
    val mergeSpawnpoints: MutableState<Boolean> = mutableStateOf(true),
    val mergeRacingCheckpoints: MutableState<Boolean> = mutableStateOf(false),
    val mergeTDMSpawnpoints: MutableState<Boolean> = mutableStateOf(false)
)
