package com.aliernfrog.lactool.util.extensions

import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.core.text.HtmlCompat.fromHtml

fun String.removeHtml(): String {
    return fromHtml(this, FROM_HTML_MODE_LEGACY).toString()
}