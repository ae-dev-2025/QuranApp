package com.quranapp.android.learning.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.quranapp.android.learning.concepts.Concept

/** Arabic letters and marks. */
private const val ARABIC = "[\u0600-\u06FF\u0750-\u077F\u08A0-\u08FF\uFB50-\uFDFF\uFE70-\uFEFF]"

/** One Arabic example: its words and the spaces between them, so a phrase stays together. */
private val ARABIC_EXAMPLE = Regex("$ARABIC+(?: $ARABIC+)*")

/** The left-to-right mark: invisible, but a letter with a direction. */
private const val LRM = "\u200E"

/**
 * Keeps the Arabic examples of an English sentence in the order they're written.
 *
 * Text is laid out by the Unicode bidirectional algorithm. A comma or colon between two
 * Arabic examples ("رَبّ, هُوَ") sits between two right-to-left letters, so it takes their
 * direction, and the examples show backwards: "هُوَ ,رَبّ". A left-to-right mark after each
 * example puts the punctuation back in the sentence's direction.
 */
internal fun arabicExamplesInOrder(text: String): String = text.replace(ARABIC_EXAMPLE) { it.value + LRM }

/**
 * An ayah's reference, "الفاتحة 1:7". Numbers after Arabic letters would join them and show
 * first ("1:7 الفاتحة"), so the surah's name keeps its place in any language.
 */
internal fun ayahReference(surahName: String, surahNo: Int, ayahNo: Int): String =
    "${arabicExamplesInOrder(surahName)} $surahNo:$ayahNo".trim()

/** A concept's title, with its Arabic examples in order ("وَ, فَـ and ثُمَّ"). */
@Composable
internal fun conceptTitle(concept: Concept): String = arabicExamplesInOrder(stringResource(concept.titleRes))
