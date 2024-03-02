package com.aliernfrog.lactool.ui.screen.maps

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.enum.MapAction
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.AppTopBar
import com.aliernfrog.lactool.ui.component.ButtonIcon
import com.aliernfrog.lactool.ui.component.FadeVisibility
import com.aliernfrog.lactool.ui.component.SettingsButton
import com.aliernfrog.lactool.ui.component.VerticalSegmentedButtons
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.component.maps.PickMapButton
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen(
    mapsViewModel: MapsViewModel = koinViewModel(),
    onNavigateSettingsRequest: () -> Unit
) {
    LaunchedEffect(mapsViewModel.chosenMap) {
        if (mapsViewModel.chosenMap == null) mapsViewModel.mapListShown = true
    }

    BackHandler(mapsViewModel.chosenMap != null) {
        mapsViewModel.chooseMap(null)
    }

    AppScaffold(
        topBar = { AppTopBar(
            title = stringResource(R.string.maps),
            scrollBehavior = it,
            actions = {
                SettingsButton(onClick = onNavigateSettingsRequest)
            }
        ) },
        topAppBarState = mapsViewModel.topAppBarState
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(mapsViewModel.scrollState)) {
            PickMapButton(
                chosenMap = mapsViewModel.chosenMap,
                showMapThumbnail = mapsViewModel.prefs.showChosenMapThumbnail
            ) {
                mapsViewModel.mapListShown = true
            }
            Actions()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Actions(
    mapsViewModel: MapsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val chosenMap = mapsViewModel.chosenMap ?: return

    TextField(
        value = mapsViewModel.mapNameEdit,
        onValueChange = { mapsViewModel.mapNameEdit = it },
        label = { Text(stringResource(R.string.maps_mapName)) },
        placeholder = { Text(mapsViewModel.chosenMap?.name ?: "") },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.TextFields,
                contentDescription = null
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(AppComponentShape)
    )

    FadeVisibility(visible = mapsViewModel.prefs.showMapNameFieldGuide) {
        OutlinedCard(
            onClick = { mapsViewModel.prefs.showMapNameFieldGuide = false },
            shape = AppComponentShape,
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                )
            ) {
                Icon(Icons.Rounded.TipsAndUpdates, contentDescription = null)
                Column(
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = stringResource(R.string.maps_mapName_guide),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = stringResource(R.string.action_tapToDismiss),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.alpha(0.7f)
                    )
                }
            }
        }
    }

    Crossfade(targetState = MapAction.RENAME.availableFor(chosenMap)) { buttonsEnabled ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 8.dp,
                    vertical = 4.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            OutlinedButton(
                onClick = { scope.launch {
                    MapAction.DUPLICATE.execute(context, chosenMap)
                } },
                enabled = buttonsEnabled
            ) {
                ButtonIcon(rememberVectorPainter(Icons.Default.FileCopy))
                Text(stringResource(R.string.maps_duplicate))
            }
            Button(
                onClick = { scope.launch {
                    MapAction.RENAME.execute(context, chosenMap)
                } },
                enabled = buttonsEnabled
            ) {
                ButtonIcon(rememberVectorPainter(Icons.Default.Edit))
                Text(stringResource(R.string.maps_rename))
            }
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).alpha(0.7f),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.surfaceVariant
    )

    val actions: List<@Composable () -> Unit> = MapAction.entries.filter { action ->
        action != MapAction.RENAME && action != MapAction.DUPLICATE
    }.map { action -> {
        FadeVisibility(visible = action.availableFor(chosenMap)) {
            ButtonRow(
                title = stringResource(action.longLabelId),
                description = action.descriptionId?.let { stringResource(it) },
                painter = rememberVectorPainter(action.icon),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = if (action.destructive) MaterialTheme.colorScheme.error
                else contentColorFor(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) { scope.launch {
                action.execute(context = context, chosenMap)
            } }
        }
    } }

    VerticalSegmentedButtons(
        *actions.toTypedArray(),
        modifier = Modifier.padding(8.dp)
    )
}
