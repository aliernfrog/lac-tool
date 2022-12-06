package com.aliernfrog.lactool.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.state.MapsEditState
import com.aliernfrog.lactool.ui.composable.LACToolFAB
import com.aliernfrog.lactool.ui.composable.LACToolMapRole
import kotlinx.coroutines.launch

@Composable
fun MapsRolesScreen(mapsEditState: MapsEditState) {
    Box(Modifier.fillMaxSize()) {
        RolesList(mapsEditState)
        AddRoleFAB(mapsEditState, Modifier.align(Alignment.BottomEnd))
    }
}

@Composable
private fun RolesList(mapsEditState: MapsEditState) {
    val scope = rememberCoroutineScope()
    val roles = mapsEditState.mapData.value?.mapRoles ?: mutableListOf()
    LazyColumn(
        state = mapsEditState.rolesLazyListState,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = stringResource(R.string.mapsRoles_showingCount).replace("%COUNT%", roles.size.toString()),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        items(roles) {
            LACToolMapRole(it) { scope.launch { mapsEditState.showRoleSheet(it) } }
        }
        item {
            Spacer(Modifier.height(70.dp))
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun AddRoleFAB(mapsEditState: MapsEditState, modifier: Modifier) {
    val scope = rememberCoroutineScope()
    LACToolFAB(
        icon = Icons.Rounded.Add,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        scope.launch { mapsEditState.addRoleSheetState.show() }
    }
}