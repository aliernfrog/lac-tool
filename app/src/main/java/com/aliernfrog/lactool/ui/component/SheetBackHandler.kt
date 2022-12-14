package com.aliernfrog.lactool.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SheetBackHandler(vararg states: ModalBottomSheetState) {
    val scope = rememberCoroutineScope()
    states.forEach { state ->
        BackHandler(state.isVisible) {
            scope.launch { state.hide() }
        }
    }
}