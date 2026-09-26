package com.quranapp.android.learning.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.quranapp.android.R
import com.quranapp.android.compose.theme.alpha

/** A small ">" that hints the row opens a page. It points left in right-to-left languages. */
@Composable
internal fun OpenChevron() {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Icon(
        painter = painterResource(R.drawable.dr_icon_chevron_down),
        contentDescription = null, // decorative: the row's click label already says what happens
        tint = colorScheme.onSurface.alpha(0.5f),
        modifier = Modifier
            .size(18.dp)
            .rotate(if (isRtl) 90f else -90f),
    )
}
