package com.aliernfrog.lactool.util.staticutil

import com.aliernfrog.lactool.data.LACMapObjectFilter
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

        fun lineMatchesObjectFilter(line: String, filter: LACMapObjectFilter): Boolean {
            val type = getEditorLineType(line)
            if (type != LACLineType.OBJECT) return false
            val objectName = type.getValue(line)
            val filterQuery = filter.query.value
            val ignoreCase = !filter.caseSensitive.value
            return if (filter.exactMatch.value) {
                objectName.equals(filterQuery, ignoreCase)
            } else {
                objectName.startsWith(filterQuery, ignoreCase)
            }
        }
    }
}