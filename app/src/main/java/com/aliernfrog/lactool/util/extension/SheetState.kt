package com.aliernfrog.lactool.util.extension

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue

/**
 * Checks if any part of the sheet is visible.
 * @return true if any part of the sheet is visible.
 */
@OptIn(ExperimentalMaterial3Api::class)
fun SheetState.isAnyVisible(): Boolean {
    return currentValue != SheetValue.Hidden || targetValue != SheetValue.Hidden
}