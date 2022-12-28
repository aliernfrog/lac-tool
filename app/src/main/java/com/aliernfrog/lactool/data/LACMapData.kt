package com.aliernfrog.lactool.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

data class LACMapData(
    var mapLines: MutableList<String>? = null,
    var serverName: MutableState<String>? = null,
    var serverNameLine: Int? = null,
    var mapType: MutableState<Int> = mutableStateOf(0),
    var mapTypeLine: Int? = null,
    var mapOptions: MutableList<LACMapOption> = mutableStateListOf(),
    var mapRoles: MutableList<String>? = null,
    var mapRolesLine: Int? = null,
    var replacableObjects: MutableList<LACMapObject> = mutableStateListOf()
)
