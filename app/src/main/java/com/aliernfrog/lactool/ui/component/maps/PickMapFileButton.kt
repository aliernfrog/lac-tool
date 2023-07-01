package com.aliernfrog.lactool.ui.component.maps

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LACMap
import com.aliernfrog.lactool.ui.component.FadeVisibility
import com.aliernfrog.lactool.ui.component.form.FormHeader
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.util.extension.clickableWithColor
import com.aliernfrog.lactool.util.extension.getDetails

@Composable
fun PickMapFileButton(
    chosenMap: LACMap?,
    showMapThumbnail: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(chosenMap?.details?.value) {
        if (chosenMap?.details?.value.isNullOrEmpty()) chosenMap?.getDetails(context)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(8.dp)
            .clip(AppComponentShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickableWithColor(
                color = MaterialTheme.colorScheme.onPrimary,
                onClick = onClick
            )
    ) {
        FadeVisibility(showMapThumbnail && chosenMap != null) {
            AsyncImage(
                model = chosenMap?.thumbnailModel,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            modifier = Modifier
                .background(Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary,
                        Color.Transparent
                    )
                ))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FormHeader(
                title = chosenMap?.name ?: stringResource(R.string.maps_pickMap),
                description = chosenMap?.getDetails(context),
                painter = rememberVectorPainter(Icons.Rounded.LocationOn),
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(56.dp)
                    .weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}