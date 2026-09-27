package com.quranapp.android.learning.ui

import android.text.TextUtils
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import com.quranapp.android.R
import com.quranapp.android.compose.theme.QuranAppTheme
import java.util.Locale
import android.util.LayoutDirection as AndroidLayoutDirection

/** The learning screens' theme: the app's own, laid out in the direction of the learning strings' language. */
@Composable
internal fun LearningTheme(content: @Composable () -> Unit) {
    QuranAppTheme { LearningDirection(content) }
}

/**
 * Learning mode's strings are only in English so far. In a right-to-left app language they
 * fall back to English, and Compose would lay that English out right to left ("?ALREADY
 * READING"), with short lines on the right and long ones on the left. So learning screens
 * follow the language their strings are written in, [R.string.learning_strings_language],
 * which each translation sets to its own: the layout and the text both. Arabic (ayahs,
 * words, letters) sets right to left itself, wherever it is shown.
 */
@Composable
internal fun LearningDirection(content: @Composable () -> Unit) {
    val language = Locale.forLanguageTag(stringResource(R.string.learning_strings_language))
    val rtl = TextUtils.getLayoutDirectionFromLocale(language) == AndroidLayoutDirection.RTL
    val text = if (rtl) TextDirection.Rtl else TextDirection.Ltr
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        shapes = MaterialTheme.shapes,
        typography = MaterialTheme.typography.withTextDirection(text),
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
            LocalTextStyle provides LocalTextStyle.current.copy(textDirection = text),
            content = content,
        )
    }
}

private fun Typography.withTextDirection(direction: TextDirection) = Typography(
    displayLarge = displayLarge.copy(textDirection = direction),
    displayMedium = displayMedium.copy(textDirection = direction),
    displaySmall = displaySmall.copy(textDirection = direction),
    headlineLarge = headlineLarge.copy(textDirection = direction),
    headlineMedium = headlineMedium.copy(textDirection = direction),
    headlineSmall = headlineSmall.copy(textDirection = direction),
    titleLarge = titleLarge.copy(textDirection = direction),
    titleMedium = titleMedium.copy(textDirection = direction),
    titleSmall = titleSmall.copy(textDirection = direction),
    bodyLarge = bodyLarge.copy(textDirection = direction),
    bodyMedium = bodyMedium.copy(textDirection = direction),
    bodySmall = bodySmall.copy(textDirection = direction),
    labelLarge = labelLarge.copy(textDirection = direction),
    labelMedium = labelMedium.copy(textDirection = direction),
    labelSmall = labelSmall.copy(textDirection = direction),
)
