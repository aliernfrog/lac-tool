package com.aliernfrog.lactool.util.extension

import androidx.compose.runtime.snapshots.SnapshotStateList

fun SnapshotStateList<*>.removeLastIfMultiple() {
    if (this.size <= 1) return
    this.removeAt(this.lastIndex)
}

fun <T> SnapshotStateList<T>.clearAndAdd(obj: T) {
    this.clear()
    this.add(obj)
}