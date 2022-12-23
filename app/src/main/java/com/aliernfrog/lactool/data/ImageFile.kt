package com.aliernfrog.lactool.data

import com.lazygeniouz.dfc.file.DocumentFileCompat

data class ImageFile(
    val name: String,
    val fileName: String,
    val file: DocumentFileCompat? = null,
    val painterModel: String = file?.uri.toString(),
)
