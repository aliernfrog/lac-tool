package com.aliernfrog.lactool.data

import com.lazygeniouz.dfc.file.DocumentFileCompat
import java.io.File

data class MapsListItem(
    val name: String,
    val fileName: String,
    val lastModified: Long,
    val file: File?,
    val documentFile: DocumentFileCompat?,
    val thumbnailPainterModel: Any? = null
)