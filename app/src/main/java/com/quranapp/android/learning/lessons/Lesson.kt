package com.quranapp.android.learning.lessons

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import com.quranapp.android.learning.concepts.ConceptCatalog
import com.quranapp.android.learning.concepts.Track

/**
 * The short lesson shown on a concept's page, in the fixed format decided for all concepts:
 * a key example, "How to spot it", "How to say it" and "Don't mix it up with". For grammar,
 * [KeyExample.youSay] is what the example means and [sayIt] is "How it works".
 *
 * All text is in string resources (res/values/learning_lessons.xml) so it can be translated.
 */
data class Lesson(
    val conceptId: String,
    val keyExample: KeyExample,
    /** Bullet points (a string-array resource). */
    @ArrayRes val spotIt: Int,
    /** Bullet points (a string-array resource). */
    @ArrayRes val sayIt: Int,
    val confusedWith: ConfusedWith? = null,
) {
    /** Grammar lessons say what the example means and how the grammar works, not how it sounds. */
    val isGrammar: Boolean get() = ConceptCatalog[conceptId]?.track == Track.GRAMMAR
}

/**
 * The words that best show a concept, and how they sound.
 *
 * The Arabic is not typed here: it is referenced by position and loaded from the app's own
 * Quran text, so it always matches exactly (see the note on Unicode order in PR 3).
 *
 * @property wordIndexes 0-based word positions in the ayah, as in `ayah_words.word_index`.
 * @property youSay a transliteration, e.g. "naʿ-bu-du".
 * @property notSay a common mistake, e.g. "na-ʿa-bu-du", or null if there isn't one.
 */
data class KeyExample(
    val surahNo: Int,
    val ayahNo: Int,
    val wordIndexes: IntRange,
    @StringRes val youSay: Int,
    @StringRes val notSay: Int? = null,
)

/** Another concept that learners mix up with this one, and how to tell them apart. */
data class ConfusedWith(val conceptId: String, @StringRes val note: Int)
