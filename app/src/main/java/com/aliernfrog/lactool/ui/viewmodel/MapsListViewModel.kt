package com.aliernfrog.lactool.ui.viewmodel

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.domain.MapsState
import com.aliernfrog.lactool.util.manager.PreferenceManager

@OptIn(ExperimentalMaterial3Api::class)
class MapsListViewModel(
    val prefs: PreferenceManager,
    private val mapsState: MapsState
) : ViewModel() {
    val availableSegments
        get() = mapsState.availableSegments
}