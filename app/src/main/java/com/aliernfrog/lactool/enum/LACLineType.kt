package com.aliernfrog.lactool.enum

enum class LACLineType(
    val ignoreWhenFiltering: Boolean = false
) {
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

    ROLES_LIST {
        private val startsWith = "Roles List:"
        override fun matches(line: String) = line.startsWith(startsWith)
        override fun getValue(line: String) = line.removePrefix(startsWith)
    },

    OPTION_NUMBER {
        override fun matches(line: String): Boolean {
            return OPTION_GENERAL.matches(line) && OPTION_GENERAL.getValue(line).toIntOrNull() != null
        }

        override fun getValue(line: String): String {
            return OPTION_GENERAL.getValue(line)
        }

        override fun getLabel(line: String): String? {
            return OPTION_GENERAL.getLabel(line)
        }
    },

    OPTION_BOOLEAN {
        override fun matches(line: String): Boolean {
            return OPTION_GENERAL.matches(line) && OPTION_GENERAL.getValue(line).toBooleanStrictOrNull() != null
        }

        override fun getValue(line: String): String {
            return OPTION_GENERAL.getValue(line)
        }

        override fun getLabel(line: String): String? {
            return OPTION_GENERAL.getLabel(line)
        }
    },

    OPTION_SWITCH {
        private val types = listOf("enabled","disabled")
        override fun matches(line: String): Boolean {
            return OPTION_GENERAL.matches(line) && types.contains(OPTION_GENERAL.getValue(line))
        }

        override fun getValue(line: String): String {
            return OPTION_GENERAL.getValue(line)
        }

        override fun getLabel(line: String): String? {
            return OPTION_GENERAL.getLabel(line)
        }
    },

    OPTION_GENERAL(ignoreWhenFiltering = true) {
        override fun matches(line: String) = line.split(": ").size == 2
        override fun getValue(line: String) = line.split(": ")[1]
        override fun getLabel(line: String) = line.split(": ")[0]
    },

    UNKNOWN(ignoreWhenFiltering = true) {
        override fun matches(line: String) = false
        override fun getValue(line: String) = "unknown"
    };

    abstract fun matches(line: String): Boolean
    abstract fun getValue(line: String): String
    open fun getLabel(line: String): String? { return null }
}