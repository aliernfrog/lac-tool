package com.aliernfrog.lactool.data

data class LACMapData(
    var mapLines: MutableList<String>? = null,
    var serverName: String? = null,
    var serverNameLine: Int? = null,
    var mapType: Int? = null,
    var mapTypeLine: Int? = null,
    var mapOptions: MutableList<LacMapOption>? = null,
    var mapRoles: MutableList<String>? = null,
    var mapRolesLine: Int? = null
)
