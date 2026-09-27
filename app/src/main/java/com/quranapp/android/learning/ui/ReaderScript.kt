package com.quranapp.android.learning.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.quranapp.android.compose.components.reader.QuranWordText
import com.quranapp.android.compose.utils.preferences.ReaderPreferences
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.db.entities.quran.AyahWordEntity
import com.quranapp.android.utils.reader.QuranScriptUtils
import com.quranapp.android.utils.reader.atlas.AtlasGlyphPlacement
import com.quranapp.android.utils.reader.atlas.LocalQuranAtlasBundle
import com.quranapp.android.utils.reader.atlas.QuranAtlasBundle
import com.quranapp.android.utils.reader.atlas.QuranAtlasLoader
import com.quranapp.android.utils.reader.atlas.getForWord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * An ayah's words as the reader shows them in Indo-Pak: the same words, by the same word
 * index, drawn from the reader's atlas. Learning screens use it to show the Quran in the
 * reader's script; the analysis still reads the Uthmani text, so highlights line up.
 */
internal class ScriptAyah(
    private val words: Map<Int, AyahWordEntity>,
    private val placements: Map<Int, List<AtlasGlyphPlacement>>,
    val bundle: QuranAtlasBundle,
) {
    fun word(index: Int): AyahWordEntity? = words[index]

    fun placementsFor(word: AyahWordEntity): List<AtlasGlyphPlacement>? = placements.getForWord(word)
}

internal object ReaderScript {
    /**
     * The ayah in [script], or null to use the app's Uthmani text and font: for Uthmani itself,
     * the King Fahd fonts (which draw whole pages, not words), or while the Indo-Pak atlas
     * hasn't been downloaded.
     */
    suspend fun ayah(context: Context, surahNo: Int, ayahNo: Int, script: String): ScriptAyah? = withContext(Dispatchers.IO) {
        if (script != QuranScriptUtils.SCRIPT_DK_INDOPAK) return@withContext null
        val verse = DatabaseProvider.getQuranRepository(context).getVerseWithDetails(surahNo, ayahNo, script, arabicEnabled = true)
            ?: return@withContext null
        val bundle = QuranAtlasLoader.getBundle(context, DatabaseProvider.getExternalQuranDatabase(context), script)
            ?: return@withContext null
        ScriptAyah(verse.words.associateBy { it.wordIndex }, bundle.getPlacementsForWords(verse.words, verse.pageNo), bundle)
    }
}

/** The ayah in the reader's script; null while it loads, and whenever the app's Uthmani text is used. */
@Composable
internal fun rememberScriptAyah(surahNo: Int, ayahNo: Int): ScriptAyah? {
    val context = LocalContext.current
    val script = ReaderPreferences.observeQuranScript()
    val ayah by produceState<ScriptAyah?>(initialValue = null, surahNo, ayahNo, script) {
        value = ReaderScript.ayah(context, surahNo, ayahNo, script)
    }
    return ayah
}

/** One word of an ayah: from [script] when there is one, else [text] in the app's Uthmanic font. */
@Composable
internal fun LearningWord(
    script: ScriptAyah?,
    index: Int,
    text: String,
    arabicFont: FontFamily,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val word = script?.word(index)
    if (script != null && word != null) {
        CompositionLocalProvider(LocalQuranAtlasBundle provides script.bundle) {
            QuranWordText(modifier = modifier, word = word, atlasPlacements = script.placementsFor(word), style = style.copy(color = color))
        }
    } else {
        Text(text = text, fontFamily = arabicFont, style = style, color = color, modifier = modifier)
    }
}

/** Some of an ayah's words, right to left, in the reader's script; [highlight] styles each word. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LearningWords(
    script: ScriptAyah?,
    words: List<String>,
    indexes: Iterable<Int>,
    arabicFont: FontFamily,
    style: TextStyle,
    color: (Int) -> Color,
    modifier: Modifier = Modifier,
    wordModifier: (Int) -> Modifier = { Modifier },
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            for (index in indexes) {
                val text = words.getOrNull(index) ?: continue
                LearningWord(script, index, text, arabicFont, style, color(index), wordModifier(index))
            }
        }
    }
}
