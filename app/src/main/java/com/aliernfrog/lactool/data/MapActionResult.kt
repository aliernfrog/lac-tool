package com.aliernfrog.lactool.data

import androidx.annotation.StringRes
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.FileWrapper

data class MapActionResult(
    val successful: Boolean,
    @StringRes val message: Int? = if (successful) null else R.string.warning_error,
    val newFile: FileWrapper? = null
)