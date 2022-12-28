package com.aliernfrog.lactool.util.staticutil

import com.aliernfrog.lactool.enum.LACLineType
import com.aliernfrog.lactool.enum.LACOldObject

class LACUtil {
    companion object {
        fun getEditorLineType(line: String): LACLineType {
            return LACLineType.values().filter { !it.ignoreWhenFiltering }
                .find { it.matches(line) } ?: LACLineType.UNKNOWN
        }

        fun findReplacementForObject(line: String): LACOldObject? {
            val objectName = line.split(":")[0]
            return LACOldObject.values().find { it.objectName == objectName }
        }
    }
}