package com.aliernfrog.lactool.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.enum.ListSorting
import com.aliernfrog.lactool.enum.ListStyle
import com.aliernfrog.lactool.ui.component.form.DividerRow
import com.aliernfrog.lactool.util.manager.base.BasePreferenceManager

@Composable
fun ListViewOptionsDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    sorting: ListSorting,
    onSortingChange: (ListSorting) -> Unit,
    sortingReversed: Boolean,
    onSortingReversedChange: (Boolean) -> Unit,
    style: ListStyle,
    onStyleChange: (ListStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.list_sorting),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        ListSorting.entries.forEach { option ->
            DropdownMenuItem(
                text = { Text(stringResource(option.label)) },
                leadingIcon = {
                    Icon(
                        imageVector = option.iconVector,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    RadioButton(
                        selected = option == sorting,
                        onClick = { onSortingChange(option) }
                    )
                },
                onClick = { onSortingChange(option) }
            )
        }
        DropdownMenuItem(
            text = { Text(stringResource(R.string.list_sorting_reversed)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = null
                )
            },
            trailingIcon = {
                Checkbox(
                    checked = sortingReversed,
                    onCheckedChange = { onSortingReversedChange(it) }
                )
            },
            onClick = { onSortingReversedChange(!sortingReversed) }
        )
        DividerRow(Modifier.padding(vertical = 4.dp))
        Text(
            text = stringResource(R.string.list_style),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        ListStyle.entries.forEach { option ->
            DropdownMenuItem(
                text = { Text(stringResource(option.label)) },
                leadingIcon = {
                    Icon(
                        imageVector = option.iconVector,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    RadioButton(
                        selected = option == style,
                        onClick = { onStyleChange(option) }
                    )
                },
                onClick = { onStyleChange(option) }
            )
        }
    }
}

@Composable
fun ListViewOptionsDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    sortingPref: BasePreferenceManager.Preference<Int>,
    sortingReversedPref: BasePreferenceManager.Preference<Boolean>,
    stylePref: BasePreferenceManager.Preference<Int>,
    modifier: Modifier = Modifier
) {
    ListViewOptionsDropdown(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        sorting = ListSorting.entries[sortingPref.value],
        onSortingChange = { sortingPref.value = it.ordinal },
        sortingReversed = sortingReversedPref.value,
        onSortingReversedChange = { sortingReversedPref.value = it },
        style = ListStyle.entries[stylePref.value],
        onStyleChange = { stylePref.value = it.ordinal },
        modifier = modifier
    )
}