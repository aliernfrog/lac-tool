package com.aliernfrog.lactool.data

import com.aliernfrog.lactool.R

data class MapActionResult(
    val successful: Boolean,
    val messageId: Int? = if (successful) null else R.string.warning_error,
    val newFile: Any? = null
)