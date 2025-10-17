package com.aliernfrog.lactool.util.manager.base

import android.content.SharedPreferences
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import com.aliernfrog.lactool.enum.ListSorting
import com.aliernfrog.lactool.enum.ListStyle
import kotlin.reflect.KProperty

@Suppress("SameParameterValue", "MemberVisibilityCanBePrivate")
abstract class BasePreferenceManager(
    private val prefs: SharedPreferences
) {
    private fun getString(key: String, defaultValue: String?) = prefs.getString(key, defaultValue)!!
    private fun getBoolean(key: String, defaultValue: Boolean) = prefs.getBoolean(key, defaultValue)
    private fun getInt(key: String, defaultValue: Int) = prefs.getInt(key, defaultValue)
    private fun getLong(key: String, defaultValue: Long) = prefs.getLong(key, defaultValue)

    private fun putString(key: String, value: String?) = prefs.edit { putString(key, value) }
    private fun putBoolean(key: String, value: Boolean) = prefs.edit { putBoolean(key, value) }
    private fun putInt(key: String, value: Int) = prefs.edit { putInt(key, value) }
    private fun putLong(key: String, value: Long) = prefs.edit { putLong(key, value) }

    val experimentalPrefs = mutableListOf<Preference<*>>()
    val debugInfoPrefs = mutableListOf<Preference<*>>()

    class Preference<T>(
        val key: String,
        val defaultValue: T,
        getter: (key: String, defaultValue: T) -> T,
        private val setter: (key: String, newValue: T) -> Unit
    ) {
        private var mutableValue by mutableStateOf(getter(key, defaultValue))
        var value: T
            get() = mutableValue
            set(value) {
                mutableValue = value
                setter(key, value)
            }

        operator fun getValue(thisRef: Any?, property: KProperty<*>) = value
        operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
            value = newValue
            setter(key, newValue)
        }
        fun resetValue() {
            value = defaultValue
        }
    }

    data class WindowSizeClassValueGroup<T>(
        val compact: T,
        val medium: T,
        val expanded: T
    ) {
        @Suppress("unused")
        @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
        @Composable
        fun getCurrent(): T {
            val windowSizeClass = calculateWindowSizeClass(LocalActivity.current!!)
            return when (windowSizeClass.widthSizeClass) {
                WindowWidthSizeClass.Compact -> compact
                WindowWidthSizeClass.Medium -> medium
                WindowWidthSizeClass.Expanded -> expanded
                else -> compact
            }
        }
    }

    class WindowSizeClassPreferenceGroup<T>(
        val key: String,
        val defaultValues: WindowSizeClassValueGroup<T>,
        private val getter: (key: String, defaultValue: T) -> T,
        private val setter: (key: String, newValue: T) -> Unit
    ) {
        private fun createSubPreference(suffix: String, defaultValue: T): Preference<T> = Preference(
            key = "$key.$suffix",
            defaultValue = defaultValue,
            getter, setter
        )

        val compact = createSubPreference("compact", defaultValues.compact)
        val medium = createSubPreference("medium", defaultValues.medium)
        val expanded = createSubPreference("expanded", defaultValues.expanded)

        @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
        @Composable
        fun getCurrent(): Preference<T> {
            val windowSizeClass = calculateWindowSizeClass(LocalActivity.current!!)
            return when (windowSizeClass.widthSizeClass) {
                WindowWidthSizeClass.Compact -> compact
                WindowWidthSizeClass.Medium -> medium
                WindowWidthSizeClass.Expanded -> expanded
                else -> compact
            }
        }
    }

    class ListViewOptionsPreference(
        val key: String,
        val defaultSorting: Int,
        val defaultSortingReversed: Boolean,
        val defaultListStyles: WindowSizeClassValueGroup<Int>,
        val defaultGridMaxLineSpans: WindowSizeClassValueGroup<Int>,
        private val intGetter: (key: String, defaultValue: Int) -> Int,
        private val intSetter: (key: String, newValue: Int) -> Unit,
        private val booleanGetter: (key: String, defaultValue: Boolean) -> Boolean,
        private val booleanSetter: (key: String, newValue: Boolean) -> Unit
    ) {
        private fun <PreferenceT> createSubPreference(
            suffix: String,
            defaultValue: PreferenceT,
            getter: (key: String, defaultValue: PreferenceT) -> PreferenceT,
            setter: (key: String, newValue: PreferenceT) -> Unit
        ): Preference<PreferenceT> = Preference(
            key = "$key.$suffix",
            defaultValue = defaultValue,
            getter, setter
        )

        private fun createSubIntPreference(suffix: String, defaultValue: Int): Preference<Int> = createSubPreference(
            suffix = suffix,
            defaultValue = defaultValue,
            intGetter, intSetter
        )

        private fun createSubBooleanPreference(suffix: String, defaultValue: Boolean): Preference<Boolean> = createSubPreference(
            suffix = suffix,
            defaultValue = defaultValue,
            booleanGetter, booleanSetter
        )

        private fun <SubT> createSubScreenSizeClassPreferenceGroup(
            suffix: String,
            defaultValues: WindowSizeClassValueGroup<SubT>,
            getter: (key: String, defaultValue: SubT) -> SubT,
            setter: (key: String, newValue: SubT) -> Unit
        ): WindowSizeClassPreferenceGroup<SubT> = WindowSizeClassPreferenceGroup(
            key = "$key.$suffix",
            defaultValues = defaultValues,
            getter, setter
        )

        val sorting = createSubIntPreference("sorting", defaultSorting)
        val sortingReversed = createSubBooleanPreference("sortingReversed", defaultSortingReversed)
        val styleGroup = createSubScreenSizeClassPreferenceGroup(
            suffix = "style",
            defaultValues = defaultListStyles,
            intGetter, intSetter
        )
        val gridMaxLineSpanGroup = createSubScreenSizeClassPreferenceGroup(
            suffix = "gridMaxLineSpan",
            defaultValues = defaultGridMaxLineSpans,
            intGetter, intSetter
        )
    }

    protected fun stringPreference(
        key: String,
        defaultValue: String = "",
        experimental: Boolean = false,
        includeInDebugInfo: Boolean = experimental
    ): Preference<String> {
        val pref = Preference(
            key = key,
            defaultValue = defaultValue,
            getter = ::getString,
            setter = ::putString
        )
        if (experimental) experimentalPrefs.add(pref)
        if (includeInDebugInfo) debugInfoPrefs.add(pref)
        return pref
    }

    protected fun booleanPreference(
        key: String,
        defaultValue: Boolean,
        experimental: Boolean = false,
        includeInDebugInfo: Boolean = experimental
    ): Preference<Boolean> {
        val pref = Preference(
            key = key,
            defaultValue = defaultValue,
            getter = ::getBoolean,
            setter = ::putBoolean
        )
        if (experimental) experimentalPrefs.add(pref)
        if (includeInDebugInfo) debugInfoPrefs.add(pref)
        return pref
    }

    protected fun intPreference(
        key: String,
        defaultValue: Int,
        experimental: Boolean = false,
        includeInDebugInfo: Boolean = experimental
    ): Preference<Int> {
        val pref = Preference(
            key = key,
            defaultValue = defaultValue,
            getter = ::getInt,
            setter = ::putInt
        )
        if (experimental) experimentalPrefs.add(pref)
        if (includeInDebugInfo) debugInfoPrefs.add(pref)
        return pref
    }

    protected fun longPreference(
        key: String,
        defaultValue: Long,
        experimental: Boolean = false,
        includeInDebugInfo: Boolean = experimental
    ): Preference<Long> {
        val pref = Preference(
            key = key,
            defaultValue = defaultValue,
            getter = ::getLong,
            setter = ::putLong
        )
        if (experimental) experimentalPrefs.add(pref)
        if (includeInDebugInfo) debugInfoPrefs.add(pref)
        return pref
    }

    protected fun listViewOptionsPreference(
        key: String,
        defaultSorting: Int = ListSorting.ALPHABETICAL.ordinal,
        defaultSortingReversed: Boolean = false,
        defaultListStyles: WindowSizeClassValueGroup<Int> = WindowSizeClassValueGroup(
            compact = ListStyle.GRID.ordinal,
            medium = ListStyle.GRID.ordinal,
            expanded = ListStyle.GRID.ordinal
        ),
        defaultGridMaxLineSpans: WindowSizeClassValueGroup<Int> = WindowSizeClassValueGroup(
            compact = 2,
            medium = 4,
            expanded = 5
        )
    ): ListViewOptionsPreference = ListViewOptionsPreference(
        key = key,
        defaultSorting = defaultSorting,
        defaultSortingReversed = defaultSortingReversed,
        defaultListStyles = defaultListStyles,
        defaultGridMaxLineSpans = defaultGridMaxLineSpans,
        intGetter = ::getInt,
        intSetter = ::putInt,
        booleanGetter = ::getBoolean,
        booleanSetter = ::putBoolean
    )
}