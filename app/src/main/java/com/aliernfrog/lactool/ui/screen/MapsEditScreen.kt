package com.aliernfrog.lactool.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aliernfrog.lactool.state.MapsEditState
import com.aliernfrog.lactool.ui.composable.LACToolFAB
import kotlinx.coroutines.launch

@Composable
fun MapsEditScreen(mapsEditState: MapsEditState, navController: NavController) {
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize()) {
        Actions(mapsEditState)
        LACToolFAB(
            icon = Icons.Default.Done,
            modifier = Modifier.align(Alignment.BottomEnd),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            scope.launch { mapsEditState.finishEditing(navController) }
        }
    }
}

@Composable
private fun Actions(mapsEditState: MapsEditState) {
    Column(Modifier.animateContentSize().verticalScroll(mapsEditState.scrollState)) {
        Text(mapsEditState.mapLines?.joinToString("\n") ?: "", Modifier.padding(horizontal = 8.dp))
    }
}