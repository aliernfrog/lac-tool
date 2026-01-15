package com.aliernfrog.lactool.ui.viewmodel

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliernfrog.lactool.util.manager.PreferenceManager
import io.github.aliernfrog.pftool_shared.data.MapsListSegment
import io.github.aliernfrog.pftool_shared.data.getDefaultMapsListSegments
import io.github.aliernfrog.pftool_shared.repository.MapRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MapsListViewModel(
    val prefs: PreferenceManager,
    private val mapRepository: MapRepository
) : ViewModel() {
    val availableSegments = mutableStateListOf<MapsListSegment>()

    init {
        viewModelScope.launch {
            mapRepository.sharedMaps.collect { maps ->
                val showSharedMapsSegment = maps.isNotEmpty()

                availableSegments.clear()
                availableSegments.addAll(getDefaultMapsListSegments(
                    includeSharedMapsSegment = showSharedMapsSegment
                ))
            }
        }
    }
}