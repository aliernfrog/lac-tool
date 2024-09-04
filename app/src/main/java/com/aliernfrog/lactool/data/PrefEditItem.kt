package com.aliernfrog.lactool.data

import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.manager.base.BasePreferenceManager

data class PrefEditItem<T>(
    val preference: (PreferenceManager) -> BasePreferenceManager.Preference<T>,
    val label: (PreferenceManager) -> Any = { preference(it).key }
)