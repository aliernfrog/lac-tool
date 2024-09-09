package com.aliernfrog.lactool.data

data class ReleaseInfo(
    val versionName: String,
    val versionCode: Long,
    val preRelease: Boolean,
    val body: String,
    val htmlUrl: String,
    val downloadLink: String
)
