package com.aliernfrog.lactool.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.MediaViewData
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaView(
    data: MediaViewData,
    onDismissRequest: () -> Unit
) {
    val density = LocalDensity.current
    val zoomState = rememberZoomState()
    val isZoomedIn = zoomState.scale > 1f
    val bottomSheetState = rememberStandardBottomSheetState(
        confirmValueChange = {
            !(it == SheetValue.Hidden && !isZoomedIn)
        },
        skipHiddenState = false
    )

    var viewportHeight by remember { mutableStateOf(0.dp) }
    var offsetY by remember { mutableStateOf(0.dp) }
    val animatedOffsetY by animateDpAsState(offsetY)

    LaunchedEffect(offsetY) {
        if (data.options == null) return@LaunchedEffect bottomSheetState.hide()
        (offsetY == 0.dp).let { show ->
            if (show) bottomSheetState.partialExpand()
            else bottomSheetState.hide()
        }
    }
    
    LaunchedEffect(isZoomedIn) {
        if (data.options == null) return@LaunchedEffect bottomSheetState.hide()
        (!isZoomedIn).let { show ->
            if (show) bottomSheetState.partialExpand()
            else bottomSheetState.hide()
        }
    }

    BottomSheetScaffold(
        sheetContent = {
            data.options?.let {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                ) {
                    it.invoke()
                }
            }
        },
        sheetPeekHeight = viewportHeight/3,
        scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = bottomSheetState
        ),
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged {
                with(density) {
                    viewportHeight = it.height.toDp()
                }
            },
        containerColor = Color.Black.copy(
            alpha = (viewportHeight.value/offsetY.value.absoluteValue/11)
        ),
        contentColor = Color.White
    ) {
        Box {
            AnimatedVisibility(
                visible = !isZoomedIn,
                modifier = Modifier.zIndex(1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .statusBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.action_close)
                        )
                    }
                    data.title?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
            AsyncImage(
                model = data.model,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .zoomable(zoomState)
                    .offset { IntOffset(x = 0, y = animatedOffsetY.roundToPx()) }
                    .pointerInput(Unit) {
                        if (!isZoomedIn) detectVerticalDragGestures(
                            onDragEnd =  {
                                if (zoomState.scale <= 1f && offsetY.value.absoluteValue > viewportHeight.value/7) onDismissRequest()
                                offsetY = 0.dp
                            },
                            onVerticalDrag = { _, dragAmount ->
                                if (zoomState.scale <= 1f) with(density) {
                                    offsetY += dragAmount.toDp()
                                }
                            }
                        )
                    }
            )
        }
    }
}