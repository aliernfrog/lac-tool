package com.aliernfrog.lactool.impl.laclib

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aliernfrog.laclib.data.LACMapDownloadableMaterial
import com.aliernfrog.laclib.data.LACMapObject
import com.aliernfrog.laclib.enum.LACMapType
import com.aliernfrog.laclib.map.LACMapEditor

class MapEditorState(
    val editor: LACMapEditor
) {
    private var _serverName by mutableStateOf(editor.serverName)
    var serverName: String?
        get() = _serverName
        set(value) {
            _serverName = value
            editor.serverName = value
        }

    private var _mapType by mutableStateOf(editor.mapType)
    var mapType: LACMapType?
        get() = _mapType
        set(value) {
            _mapType = value
            editor.mapType = value
        }

    private var _mapRoles by mutableStateOf(editor.mapRoles?.toList())
    var mapRoles: List<String>?
        get() = _mapRoles
        set(value) {
            _mapRoles = value
            editor.mapRoles = value?.toMutableList()
        }

    private var _mapOptions by mutableStateOf(editor.mapOptions.map { MutableMapOption(it) })
    var mapOptions: List<MutableMapOption>
        get() = _mapOptions
        set(value) {
            _mapOptions = value
            editor.mapOptions = value.map { it.toImmutable() }.toMutableList()
        }

    fun pushMapOptionsState() {
        editor.mapOptions = mapOptions.map { it.toImmutable() }.toMutableList()
    }

    private var _replaceableObjects by mutableStateOf(editor.replacableObjects.toList())
    var replaceableObjects: List<LACMapObject>
        get() = _replaceableObjects
        set(value) {
            _replaceableObjects = value
            editor.replacableObjects = value.toMutableList()
        }

    private var _downloadableMaterials by mutableStateOf(editor.downloadableMaterials.toList())
    var downloadableMaterials: List<LACMapDownloadableMaterial>
        get() = _downloadableMaterials
        set(value) {
            _downloadableMaterials = value
            editor.downloadableMaterials = value.toMutableList()
        }

    fun replaceOldObjects(): Int {
        val replaced = editor.replaceOldObjects()
        _replaceableObjects = editor.replacableObjects.toList()
        return replaced
    }

    fun addRole(
        role: String,
        onIllegalChar: (String) -> Unit,
        onSuccess: () -> Unit
    ) {
        editor.addRole(
            role = role,
            onIllegalChar = onIllegalChar,
            onSuccess = onSuccess
        )
        _mapRoles = editor.mapRoles?.toList()
    }

    fun deleteRole(role: String) {
        editor.deleteRole(role)
        _mapRoles = editor.mapRoles?.toList()
    }

    fun removeDownloadableMaterial(url: String): Int? {
        val removed = editor.removeDownloadableMaterial(url)
        _downloadableMaterials = editor.downloadableMaterials.toList()
        return removed
    }
}