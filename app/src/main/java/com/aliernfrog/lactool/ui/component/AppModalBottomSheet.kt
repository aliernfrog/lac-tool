package com.aliernfrog.lactool.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliernfrog.lactool.ui.theme.AppBottomSheetShape
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppModalBottomSheet(
    title: String? = null,
    sheetState: ModalBottomSheetState,
    sheetScrollState: ScrollState = rememberScrollState(),
    sheetContent: @Composable ColumnScope.() -> Unit
) {
    BaseModalBottomSheet(sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppBottomSheetShape)
                .verticalScroll(sheetScrollState)
        ) {
            if (title != null) Text(
                text = title,
                fontSize = 30.sp,
                modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally)
            )
            sheetContent()
            Spacer(Modifier.systemBarsPadding())
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun BaseModalBottomSheet(
    sheetState: ModalBottomSheetState,
    sheetContent: @Composable ColumnScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    ModalBottomSheetLayout(
        sheetBackgroundColor = Color.Transparent,
        sheetContentColor = MaterialTheme.colorScheme.onBackground,
        sheetState = sheetState,
        sheetElevation = 0.dp,
        content = {},
        sheetContent = {
            Surface(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .shadow(16.dp, AppBottomSheetShape)
                    .clip(AppBottomSheetShape)
                    .imePadding(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .size(32.dp, 4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            .align(Alignment.CenterHorizontally)
                    )
                    sheetContent()
                }
            }
        }
    )

    LaunchedEffect(sheetState.isVisible) {
        keyboardController?.hide()
    }

    BackHandler(sheetState.isVisible) {
        scope.launch { sheetState.hide() }
    }
}