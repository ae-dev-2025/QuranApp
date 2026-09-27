package com.quranapp.android.learning.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import com.quranapp.android.R

/** A section's title: TalkBack users can jump from heading to heading. */
internal fun Modifier.heading(): Modifier = semantics { heading() }

/**
 * [size] at the default font size, growing with the font size chosen in Android's settings, for
 * boxes that hold text (a step's number, a letter, a column of labels) so large text isn't cut off.
 */
@Composable
internal fun grownWithText(size: Dp): Dp = with(LocalDensity.current) { max(size, size.value.sp.toDp()) }

/**
 * The checkbox that marks a word or rule known, in rows whose own tap opens something else.
 * On its own, TalkBack reads only "checkbox, not checked", so it's labelled with what it
 * marks: "I know said, say".
 */
@Composable
internal fun KnownCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit, name: String, modifier: Modifier = Modifier.padding(4.dp)) {
    val label = stringResource(R.string.learning_i_know_name, name)
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.semantics { contentDescription = label },
    )
}
