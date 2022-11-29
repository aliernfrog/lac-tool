package com.aliernfrog.lactool.enum

enum class LACLineType {
    SERVER_NAME {
        private val startsWith = "Map Name:"
        override fun matches(line: String) = line.startsWith(startsWith)
        override fun getValue(line: String) = line.removePrefix(startsWith)
    },

    MAP_TYPE {
        private val startsWith = "Map Type:"
        override fun matches(line: String) = line.startsWith(startsWith)
        override fun getValue(line: String) = line.removePrefix(startsWith)
    },

    UNKNOWN {
        override fun matches(line: String) = false
        override fun getValue(line: String) = "unknown"
    };

    abstract fun matches(line: String): Boolean
    abstract fun getValue(line: String): String
}