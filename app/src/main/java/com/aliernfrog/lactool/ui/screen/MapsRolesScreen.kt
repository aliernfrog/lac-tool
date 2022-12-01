package com.aliernfrog.lactool.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.state.MapsEditState
import com.aliernfrog.lactool.ui.composable.LACToolMapRole
import kotlinx.coroutines.launch

@Composable
fun MapsRolesScreen(mapsEditState: MapsEditState) {
    val scope = rememberCoroutineScope()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = mapsEditState.rolesLazyListState
    ) {
        item {
            Text(
                text = stringResource(R.string.mapsRoles_showingCount).replace("%COUNT%", mapsEditState.mapRoles?.size.toString()),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        items(mapsEditState.mapRoles ?: mutableListOf()) {
            LACToolMapRole(it) { scope.launch { mapsEditState.showRoleSheet(it) } }
        }
    }
}