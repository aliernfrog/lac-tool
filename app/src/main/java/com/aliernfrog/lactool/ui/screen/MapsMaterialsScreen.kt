package com.aliernfrog.lactool.ui.screen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.state.MapsEditState
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.ImageButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsMaterialsScreen(mapsEditState: MapsEditState, navController: NavController) {
    AppScaffold(
        title = stringResource(R.string.mapsMaterials),
        topAppBarState = mapsEditState.materialsTopAppBarState,
        onBackClick = {
            navController.popBackStack()
        }
    ) {
        val materials = mapsEditState.mapEditor?.downloadableMaterials ?: listOf()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = mapsEditState.materialsLazyListState
        ) {
            //TODO handle failed or unused materials
            items(materials) {
                ImageButton(
                    model = it.url,
                    title = it.name,
                    description = stringResource(R.string.mapsMaterials_usedCount).replace("%n", it.usedBy.size.toString())
                ) {
                    //TODO
                }
            }
            item {
                Spacer(Modifier.systemBarsPadding())
            }
        }
    }
}