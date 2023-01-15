package com.aliernfrog.lactool.data

import com.lazygeniouz.dfc.file.DocumentFileCompat
import java.io.File

data class LACMap(
    val name: String,
    val fileName: String,
    val fileSize: Long? = null,
    val lastModified: Long? = null,
    val file: File? = null,
    val documentFile: DocumentFileCompat? = null,
    val thumbnailPainterModel: Any? = null
)