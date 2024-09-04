package com.aliernfrog.lactool.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pinch
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ZoomInMap
import androidx.compose.material.icons.rounded.HideImage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.MediaViewData
import com.aliernfrog.lactool.ui.component.form.DividerRow
import com.aliernfrog.lactool.ui.viewmodel.InsetsViewModel
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaView(
    data: MediaViewData,
    onDismissRequest: () -> Unit
) {
    val mainViewModel = koinViewModel<MainViewModel>()
    val insetsViewModel = koinViewModel<InsetsViewModel>()

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val zoomState = rememberZoomState()
    val isZoomedIn = zoomState.scale > 1f
    val isIMEShown = insetsViewModel.imePadding > 0.dp
    val bottomSheetState = rememberStandardBottomSheetState(
        skipHiddenState = false
    )

    var state by remember { mutableStateOf(
        if (data.model != null) MediaViewState.SUCCESS else MediaViewState.NO_IMAGE
    ) }
    var showOverlay by remember { mutableStateOf(false) }
    var viewportHeight by remember { mutableStateOf(0.dp) }
    var optionsHeight by remember { mutableStateOf(viewportHeight/3) }
    var offsetY by remember { mutableStateOf(0.dp) }
    val animatedOffsetY by animateDpAsState(offsetY)
    val overlayCanBeShown = !isZoomedIn && offsetY == 0.dp
    val sheetPeekHeight = (viewportHeight/3).let { maxPeekHeight ->
        if (optionsHeight == 0.dp || optionsHeight > maxPeekHeight) maxPeekHeight
        else optionsHeight
    }
    
    BackHandler {
        if (isZoomedIn) scope.launch { zoomState.reset() }
        else onDismissRequest()
    }
    
    LaunchedEffect(overlayCanBeShown) {
        showOverlay = overlayCanBeShown
        if (data.options != null && overlayCanBeShown && bottomSheetState.targetValue == SheetValue.Hidden) bottomSheetState.partialExpand()
    }
    
    LaunchedEffect(showOverlay) {
        if (data.options == null) return@LaunchedEffect bottomSheetState.hide()
        if (showOverlay && overlayCanBeShown && bottomSheetState.targetValue == SheetValue.Hidden) bottomSheetState.partialExpand()
        else if (!showOverlay && bottomSheetState.targetValue != SheetValue.Hidden) bottomSheetState.hide()
    }

    // Expand sheet to add IME padding, if IME is shown
    LaunchedEffect(isIMEShown) {
        if (isIMEShown) bottomSheetState.expand()
    }

    BottomSheetScaffold(
        sheetContent = {
            data.options?.let {
                Column(
                    modifier = Modifier
                        .onSizeChanged {
                            with(density) {
                                optionsHeight = it.height.toDp()+48.dp // 48dp drag handle height
                            }
                        }
                        .navigationBarsPadding()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                ) {
                    it.invoke()
                }
            }
        },
        sheetPeekHeight = sheetPeekHeight,
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
            FadeVisibility(
                visible = showOverlay && overlayCanBeShown,
                modifier = Modifier.zIndex(1f)
            ) {
                Row(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f))
                        .statusBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
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
                                style = MaterialTheme.typography.titleLarge,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 2
                            )
                        }
                    }
                    IconButton(
                        onClick = { mainViewModel.prefs.showMediaViewGuide.value = true },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = stringResource(R.string.mediaView_guide)
                        )
                    }
                }
            }
            Crossfade(
                targetState = state,
                modifier = Modifier
                    .fillMaxSize()
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
            ) {
                @Composable
                fun CenteredBox(
                    modifier: Modifier = Modifier,
                    content: @Composable BoxScope.() -> Unit
                ) {
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                        content = content
                    )
                }

                when (it) {
                    MediaViewState.ERROR -> CenteredBox {
                        data.errorContent()
                    }
                    MediaViewState.NO_IMAGE -> CenteredBox {
                        ErrorWithIcon(
                            error = "",
                            painter = rememberVectorPainter(Icons.Rounded.HideImage),
                            contentColor = Color.White
                        )
                    }
                    MediaViewState.SUCCESS -> AsyncImage(
                        model = data.model,
                        onError = { state = MediaViewState.ERROR },
                        onSuccess = { state = MediaViewState.SUCCESS },
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .zoomable(
                                zoomState = zoomState,
                                zoomEnabled = data.zoomEnabled,
                                onTap = { _ ->
                                    showOverlay = !showOverlay && overlayCanBeShown
                                }
                            )
                    )
                }
            }
        }
    }

    if (mainViewModel.prefs.showMediaViewGuide.value) GuideDialog(
        onDismissRequest = {
            mainViewModel.prefs.showMediaViewGuide.value = false
        }
    )
}

@Composable
private fun GuideDialog(
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(stringResource(R.string.action_ok))
            }
        },
        text = {
            Column {
                listOf(
                    Icons.Default.TouchApp to R.string.mediaView_guide_toggleOverlay,
                    Icons.Default.ZoomInMap to R.string.mediaView_guide_toggleZoom,
                    Icons.Default.Pinch to R.string.mediaView_guide_zoom
                ).forEachIndexed { index, pair ->
                    if (index != 0) DividerRow(Modifier.fillMaxWidth())
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Icon(
                            imageVector = pair.first,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Text(stringResource(pair.second))
                    }
                }
            }
        }
    )
}

private enum class MediaViewState {
    SUCCESS,
    ERROR,
    NO_IMAGE
}