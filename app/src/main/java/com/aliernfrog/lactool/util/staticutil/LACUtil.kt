package com.aliernfrog.lactool.util.staticutil

import android.annotation.SuppressLint
import android.content.Context
import com.aliernfrog.laclib.enum.LACMapLineType
import com.aliernfrog.laclib.util.LACLibUtil
import com.aliernfrog.lactool.data.LACMapToMerge
import com.aliernfrog.lactool.data.XYZ
import com.aliernfrog.lactool.util.extension.add
import com.aliernfrog.lactool.util.extension.joinToString

class LACUtil {
    companion object {
        @SuppressLint("Recycle")
        fun filterMapToMergeContent(mapToMerge: LACMapToMerge, isBaseMap: Boolean, context: Context): List<String> {
            val inputStream = if (mapToMerge.map.file != null) mapToMerge.map.file.inputStream() else context.contentResolver.openInputStream(mapToMerge.map.documentFile!!.uri)
            val lines = inputStream?.bufferedReader()?.readText()?.split("\n") ?: emptyList()
            val filtered = mutableListOf<String>()
            inputStream?.close()
            lines.forEach { line ->
                when (val type = LACLibUtil.getEditorLineType(line)) {
                    LACMapLineType.OBJECT -> {
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
                    LACMapLineType.VEHICLE -> {
                        val lineToAdd = if (isBaseMap) line else mergeLACObject(mapToMerge, line)
                        filtered.add(lineToAdd)
                    }
                    LACMapLineType.DOWNLOADABLE_MATERIAL -> filtered.add(line)
                    else -> if (isBaseMap) filtered.add(line)
                }
            }
            return filtered
        }

        private fun mergeLACObject(mapToMerge: LACMapToMerge, line: String): String {
            val split = line.split(":").toMutableList()
            val oldPosition = GeneralUtil.parseAsXYZ(split[1])!!
            val positionToAdd = GeneralUtil.parseAsXYZ(mapToMerge.mergePosition.value) ?: XYZ(0.toDouble(), 0.toDouble(), 0.toDouble())
            split[1] = oldPosition.add(positionToAdd).joinToString()
            return split.joinToString(":")
        }
    }
}