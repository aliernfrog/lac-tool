package com.aliernfrog.lactool.data

import com.lazygeniouz.dfc.file.DocumentFileCompat

data class LACMap(
    val mapName: String,
    val fileName: String,
    val filePath: String,
    val documentFile: DocumentFileCompat? = null
)