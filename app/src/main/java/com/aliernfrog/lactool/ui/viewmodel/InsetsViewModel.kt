package com.aliernfrog.lactool.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel

class InsetsViewModel : ViewModel() {
    var topPadding by mutableStateOf(0.dp)
    var bottomPadding by mutableStateOf(0.dp)
}