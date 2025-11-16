package com.aliernfrog.lactool.util.extension

import androidx.compose.runtime.snapshots.SnapshotStateList

fun SnapshotStateList<*>.removeLastWideCompatibility() {
    if (this.isEmpty()) return
    this.removeAt(this.lastIndex)
}

fun SnapshotStateList<*>.removeLastIfMultiple() {
    if (this.size <= 1) return
    this.removeLastWideCompatibility()
}