package com.aliernfrog.lactool.util.extension

import androidx.compose.ui.graphics.Color

fun Color.toHex(): String {
    return "#${this.value.toString(16).slice(2..7)}"
}