package com.aliernfrog.lactool.util.extension

import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import io.github.aliernfrog.pftool_shared.data.Language
import java.util.Locale

val Locale.fullCode: String
    get() = "$language-$country"

fun Locale.toLanguage(): Language? {
    return GeneralUtil.getLanguageFromCode(fullCode)
}