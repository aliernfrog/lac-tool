package com.aliernfrog.lactool.util.staticutil

import android.annotation.SuppressLint
import android.content.Context
import com.aliernfrog.lactool.data.LACMapObjectFilter
import com.aliernfrog.lactool.data.LACMapToMerge
import com.aliernfrog.lactool.enum.LACLineType
import com.aliernfrog.lactool.enum.LACOldObject
import com.aliernfrog.lactool.util.extension.add
import com.aliernfrog.lactool.util.extension.joinToString

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
            if (filterQuery.isBlank()) return false
            return if (filter.exactMatch.value) {
                objectName.equals(filterQuery, ignoreCase)
            } else {
                objectName.startsWith(filterQuery, ignoreCase)
            }
        }

        @SuppressLint("Recycle")
        fun filterMapToMergeContent(mapToMerge: LACMapToMerge, isBaseMap: Boolean, context: Context): List<String> {
            val inputStream = if (mapToMerge.map.file != null) mapToMerge.map.file.inputStream() else context.contentResolver.openInputStream(mapToMerge.map.documentFile!!.uri)
            val lines = inputStream?.bufferedReader()?.readText()?.split("\n") ?: emptyList()
            val filtered = mutableListOf<String>()
            inputStream?.close()
            lines.forEach { line ->
                when (val type = getEditorLineType(line)) {
                    LACLineType.OBJECT -> {
                        val name = type.getValue(line)
                        val lineToAdd = if (isBaseMap) line else mergeLACObject(mapToMerge, line)
                        if (name == "Spawn_Point_Editor") {
                            if (mapToMerge.mergeSpawnpoints.value) filtered.add(lineToAdd)
                        } else if (name.startsWith("Checkpoint_Editor")) {
                            if (mapToMerge.mergeRacingCheckpoints.value) filtered.add(lineToAdd)
                        } else if (name.startsWith("Team_")) {
                            if (mapToMerge.mergeTDMSpawnpoints.value) filtered.add(lineToAdd)
                        } else {
                            filtered.add(lineToAdd)
                        }
                    }
                    LACLineType.VEHICLE -> {
                        val lineToAdd = if (isBaseMap) line else mergeLACObject(mapToMerge, line)
                        filtered.add(lineToAdd)
                    }
                    LACLineType.DOWNLOADABLE_MATERIAL -> filtered.add(line)
                    else -> if (isBaseMap) filtered.add(line)
                }
            }
            return filtered
        }

        private fun mergeLACObject(mapToMerge: LACMapToMerge, line: String): String {
            val split = line.split(":").toMutableList()
            val oldPosition = GeneralUtil.parseAsXYZ(split[1])!!
            val positionToAdd = GeneralUtil.parseAsXYZ(mapToMerge.mergePosition.value)!!
            split[1] = oldPosition.add(positionToAdd).joinToString()
            return split.joinToString(":")
        }
    }
}