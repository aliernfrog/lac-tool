package com.aliernfrog.lactool.util.staticutil

import com.aliernfrog.lactool.enum.LACLineType

class LACUtil {
    companion object {
        fun getEditorLineType(line: String): LACLineType {
            return LACLineType.values().filter { !it.ignoreWhenFiltering }
                .find { it.matches(line) } ?: LACLineType.UNKNOWN
        }
    }
}