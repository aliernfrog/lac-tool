package com.aliernfrog.lactool.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.ui.theme.AppRoundnessSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedButtons(
    options: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
    ) {
        options.forEachIndexed { index, option ->
            val selected = selectedIndex == index
            val isStart = index == 0
            val isEnd = index+1 == options.size
            SegmentedButton(
                selected = selected,
                onClick = { onSelect(index) },
                shape = RoundedCornerShape(
                    topStart = if (isStart) AppRoundnessSize else 0.dp,
                    bottomStart = if (isStart) AppRoundnessSize else 0.dp,
                    topEnd = if (isEnd) AppRoundnessSize else 0.dp,
                    bottomEnd = if (isEnd) AppRoundnessSize else 0.dp
                )
            ) {
                Text(option)
            }
        }
    }
}