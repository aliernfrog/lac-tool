package com.aliernfrog.lactool.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.domain.MapsState
import com.aliernfrog.lactool.util.manager.PreferenceManager

class MapsListViewModel(
    val prefs: PreferenceManager,
    private val mapsState: MapsState
) : ViewModel() {
    val availableSegments
        get() = mapsState.availableSegments

    val selectedMaps
        get() = mapsState.selectedMaps
}