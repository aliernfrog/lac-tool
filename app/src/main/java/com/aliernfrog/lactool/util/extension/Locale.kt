package com.aliernfrog.lactool.util.extension

import com.aliernfrog.lactool.data.Language
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import java.util.Locale

val Locale.fullCode: String
    get() = "$language-$country"

fun Locale.toLanguage(): Language? {
    return GeneralUtil.getLanguageFromCode(fullCode)
}