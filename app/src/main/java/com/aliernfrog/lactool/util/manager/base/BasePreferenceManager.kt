package com.aliernfrog.lactool.util.manager.base

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import kotlin.reflect.KProperty

@Suppress("SameParameterValue")
abstract class BasePreferenceManager(
    private val prefs: SharedPreferences
) {
    private fun getString(key: String, defaultValue: String?) = prefs.getString(key, defaultValue)!!
    private fun getBoolean(key: String, defaultValue: Boolean) = prefs.getBoolean(key, defaultValue)
    private fun getInt(key: String, defaultValue: Int) = prefs.getInt(key, defaultValue)

    private fun putString(key: String, value: String?) = prefs.edit { putString(key, value) }
    private fun putBoolean(key: String, value: Boolean) = prefs.edit { putBoolean(key, value) }
    private fun putInt(key: String, value: Int) = prefs.edit { putInt(key, value) }


    class Preference<T>(
        val key: String,
        val defaultValue: T,
        getter: (key: String, defaultValue: T) -> T,
        private val setter: (key: String, newValue: T) -> Unit
    ) {
        var value by mutableStateOf(getter(key, defaultValue))

        operator fun getValue(thisRef: Any?, property: KProperty<*>) = value
        operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
            value = newValue
            setter(key, newValue)
        }

        fun resetValue() {
            setValue(thisRef = null, property = ::key, newValue = defaultValue)
        }
    }


    protected fun stringPreference(
        key: String,
        defaultValue: String = ""
    ) = Preference(
        key = key,
        defaultValue = defaultValue,
        getter = ::getString,
        setter = ::putString
    )

    protected fun booleanPreference(
        key: String,
        defaultValue: Boolean
    ) = Preference(
        key = key,
        defaultValue = defaultValue,
        getter = ::getBoolean,
        setter = ::putBoolean
    )

    protected fun intPreference(
        key: String,
        defaultValue: Int
    ) = Preference(
        key = key,
        defaultValue = defaultValue,
        getter = ::getInt,
        setter = ::putInt
    )
}