package com.aliernfrog.lactool.util

import com.aliernfrog.lactool.enum.LACLineType

class LACUtil {
    companion object {
        fun getEditorLineType(line: String): LACLineType {
            return LACLineType.values().find { it.matches(line) } ?: LACLineType.UNKNOWN
        }
    }
}