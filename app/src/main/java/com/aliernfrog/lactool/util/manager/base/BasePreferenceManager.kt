package com.aliernfrog.lactool.util.manager.base

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import kotlin.reflect.KProperty

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
        }
        fun resetValue() {
            value = defaultValue
        }
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
}