package com.aliernfrog.lactool.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel

class InsetsViewModel : ViewModel() {
    var topPadding by mutableStateOf(0.dp)
    var imePadding by mutableStateOf(0.dp)
    private var internalBottomPadding by mutableStateOf(0.dp)

    /**
     * Navigation bar padding.
     * If IME is shown, returns 0 dp
     */
    var bottomPadding
        get() = if (imePadding == 0.dp) internalBottomPadding else 0.dp
        set(value) { internalBottomPadding = value }
}