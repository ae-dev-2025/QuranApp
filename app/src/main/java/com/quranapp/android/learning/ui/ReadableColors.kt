package com.quranapp.android.learning.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/** WCAG AA: the contrast normal-size text needs against its background. */
internal const val TEXT_CONTRAST = 4.5f

/** The WCAG contrast ratio of two opaque colours, from 1 (the same) to 21 (black on white). */
internal fun contrast(a: Color, b: Color): Float {
    val (light, dark) = listOf(a.luminance(), b.luminance()).sortedDescending()
    return (light + 0.05f) / (dark + 0.05f)
}

/**
 * [color], or the closest colour to it that is readable as text on every one of [backgrounds]
 * and on its own light tint (the "Known" chips, the "Right" bar): made darker on light
 * backgrounds and lighter on dark ones, in small steps, so it stays the same hue.
 */
internal fun readableOn(color: Color, backgrounds: List<Color>, tintOver: Color, minimum: Float = TEXT_CONTRAST): Color {
    val towards = if (backgrounds.first().luminance() > 0.5f) Color.Black else Color.White
    for (step in 0..50) {
        val candidate = lerp(color, towards, step / 50f)
        val tint = candidate.copy(alpha = 0.12f).compositeOver(tintOver)
        if ((backgrounds + tint).all { contrast(candidate, it) >= minimum }) return candidate
    }
    return towards
}

/** The text on a [fill]: the theme's own when it's readable, else black or white, whichever reads better. */
internal fun onColorFor(fill: Color, themed: Color): Color = when {
    contrast(themed, fill) >= TEXT_CONTRAST -> themed
    contrast(Color.White, fill) >= contrast(Color.Black, fill) -> Color.White
    else -> Color.Black
}

/**
 * The theme's colours, with primary and error readable as text on the learning screens. Most
 * themes already are, and stay as they are. The default green is 3.8:1 on the light grey
 * background, and the mono theme's light-grey error is 1.4:1 on white ("Not quite" and ✗).
 */
internal fun ColorScheme.withReadableText(): ColorScheme {
    val backgrounds = listOf(surface, background)
    val primary = readableOn(primary, backgrounds, tintOver = surface)
    val error = readableOn(error, backgrounds, tintOver = surface)
    return copy(
        primary = primary,
        onPrimary = onColorFor(primary, onPrimary),
        error = error,
        onError = onColorFor(error, onError),
    )
}
