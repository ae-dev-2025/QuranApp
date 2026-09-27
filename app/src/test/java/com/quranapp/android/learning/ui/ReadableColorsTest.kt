package com.quranapp.android.learning.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.quranapp.android.compose.theme.colors.ThemeBlueColors
import com.quranapp.android.compose.theme.colors.ThemeDefaultColors
import com.quranapp.android.compose.theme.colors.ThemeMonoColors
import com.quranapp.android.compose.theme.colors.ThemePurpleColors
import com.quranapp.android.compose.theme.colors.ThemeRedColors
import com.quranapp.android.compose.theme.colors.ThemeVioletColors
import com.quranapp.android.compose.theme.colors.ThemeYellowColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadableColorsTest {
    private val themes = listOf(
        ThemeDefaultColors(), ThemeBlueColors(), ThemeMonoColors(), ThemePurpleColors(),
        ThemeRedColors(), ThemeVioletColors(), ThemeYellowColors(),
    ).flatMap { listOf(it.lightColors(), it.darkColors()) }

    @Test
    fun contrastMatchesTheWcagExamples() {
        assertEquals(21f, contrast(Color.Black, Color.White), 0.01f)
        assertEquals(1f, contrast(Color.White, Color.White), 0.01f)
    }

    @Test
    fun everyThemesTextIsReadable() {
        for (theme in themes) {
            val readable = theme.withReadableText()
            for (color in listOf(readable.primary, readable.error)) {
                assertReadable(color, readable.surface)
                assertReadable(color, readable.background)
                assertReadable(color, color.copy(alpha = 0.12f).compositeOver(readable.surface))
            }
            assertReadable(readable.onPrimary, readable.primary)
            assertReadable(readable.onError, readable.error)
        }
    }

    @Test
    fun themesThatAreReadableStayAsTheyAre() {
        val blue = ThemeBlueColors().lightColors()
        assertEquals(blue.primary, blue.withReadableText().primary)
        assertEquals(blue.error, blue.withReadableText().error)
    }

    @Test
    fun theDefaultGreenGetsDarkerOnLightAndLighterOnDark() {
        val light = ThemeDefaultColors().lightColors()
        val dark = ThemeDefaultColors().darkColors()
        assertTrue(contrast(light.primary, light.background) < TEXT_CONTRAST)
        assertTrue(light.withReadableText().primary.luminanceOf() < light.primary.luminanceOf())
        assertTrue(dark.withReadableText().primary.luminanceOf() > dark.primary.luminanceOf())
    }

    @Test
    fun theMonoErrorIsNoLongerNearlyWhite() {
        val mono = ThemeMonoColors().lightColors()
        assertTrue(contrast(mono.error, mono.surface) < 2f)
        assertNotEquals(mono.error, mono.withReadableText().error)
    }

    private fun assertReadable(text: Color, background: Color) {
        val ratio = contrast(text, background)
        assertTrue("$text on $background is $ratio", ratio >= TEXT_CONTRAST)
    }

    private fun Color.luminanceOf() = contrast(this, Color.Black)
}
