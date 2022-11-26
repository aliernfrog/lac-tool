package com.aliernfrog.lactool.ui.composable

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliernfrog.lactool.LACToolComposableShape
import com.aliernfrog.lactool.LACToolRoundnessSize
import com.aliernfrog.lactool.util.GeneralUtil

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LACToolModalBottomSheet(title: String? = null, sheetState: ModalBottomSheetState, sheetScrollState: ScrollState = rememberScrollState(), sheetContent: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheetLayout(
        sheetBackgroundColor = Color.Transparent,
        sheetContentColor = MaterialTheme.colorScheme.onBackground,
        sheetState = sheetState,
        sheetElevation = 0.dp,
        content = {},
        sheetContent = {
            Column(modifier = Modifier.statusBarsPadding().fillMaxWidth().clip(RoundedCornerShape(topStart = LACToolRoundnessSize, topEnd = LACToolRoundnessSize)).background(MaterialTheme.colorScheme.background), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier
                    .padding(vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = LACToolComposableShape)
                    .size(30.dp, 5.dp)
                    .align(Alignment.CenterHorizontally)
                )
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = LACToolRoundnessSize, topEnd = LACToolRoundnessSize)).verticalScroll(sheetScrollState)) {
                    if (title != null) Text(text = title, fontSize = 30.sp, modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally))
                    sheetContent()
                    Spacer(modifier = Modifier.height(GeneralUtil.getNavigationBarHeight()))
                }
            }
        }
    )
}