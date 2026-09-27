package com.quranapp.android.learning.letters

import androidx.annotation.StringRes
import com.quranapp.android.R

/**
 * One of the 28 letters, or hamza: the design's first kind of item. The [id] is stored in
 * the learner's progress, so it must never change once released.
 *
 * @property name The letter's name in Latin letters, as learners say it: "bāʾ".
 * @property arabicName Its name in Arabic, written for the app's Quran font (sukun is U+06E1).
 * @property soundRes A short hint at its sound, such as "b as in book".
 * @property group The design's shape group, 1 to 7: each new letter differs from a known one
 * by dots or a single stroke.
 * @property joinsNext False for the letters that never join the letter after them.
 */
data class Letter(
    val id: String,
    val char: Char,
    val name: String,
    val arabicName: String,
    @StringRes val soundRes: Int,
    val group: Int,
    val joinsNext: Boolean = true,
)

/** How a letter looks alone and at the start, middle and end of a word. */
data class LetterShapes(val alone: String, val start: String, val middle: String, val end: String)

/** The 29 letter items, in the order they are taught (decision 2's stage 0). */
object Letters {
    val all: List<Letter> = listOf(
        // 1: one base shape; the dots tell them apart
        Letter("letter.alif", 'ا', "alif", "أَلِف", R.string.letter_sound_alif, 1, joinsNext = false),
        Letter("letter.ba", 'ب', "bāʾ", "بَاء", R.string.letter_sound_ba, 1),
        Letter("letter.ta", 'ت', "tāʾ", "تَاء", R.string.letter_sound_ta, 1),
        Letter("letter.tha", 'ث', "thāʾ", "ثَاء", R.string.letter_sound_tha, 1),
        Letter("letter.nun", 'ن', "nūn", "نُون", R.string.letter_sound_nun, 1),
        Letter("letter.ya", 'ي', "yāʾ", "يَاء", R.string.letter_sound_ya, 1),
        // 2: one shape; ح and خ come from the throat
        Letter("letter.jim", 'ج', "jīm", "جِيم", R.string.letter_sound_jim, 2),
        Letter("letter.hha", 'ح', "ḥāʾ", "حَاء", R.string.letter_sound_hha, 2),
        Letter("letter.kha", 'خ', "khāʾ", "خَاء", R.string.letter_sound_kha, 2),
        // 3: never join the next letter
        Letter("letter.dal", 'د', "dāl", "دَال", R.string.letter_sound_dal, 3, joinsNext = false),
        Letter("letter.dhal", 'ذ', "dhāl", "ذَال", R.string.letter_sound_dhal, 3, joinsNext = false),
        Letter("letter.ra", 'ر', "rāʾ", "رَاء", R.string.letter_sound_ra, 3, joinsNext = false),
        Letter("letter.zay", 'ز', "zāy", "زَاي", R.string.letter_sound_zay, 3, joinsNext = false),
        Letter("letter.waw", 'و', "wāw", "وَاو", R.string.letter_sound_waw, 3, joinsNext = false),
        // 4: tooth shapes; ص and ض are heavy
        Letter("letter.sin", 'س', "sīn", "سِين", R.string.letter_sound_sin, 4),
        Letter("letter.shin", 'ش', "shīn", "شِين", R.string.letter_sound_shin, 4),
        Letter("letter.sad", 'ص', "ṣād", "صَاد", R.string.letter_sound_sad, 4),
        Letter("letter.dad", 'ض', "ḍād", "ضَاد", R.string.letter_sound_dad, 4),
        // 5: ط and ظ are heavy; ع and غ come from the throat
        Letter("letter.tta", 'ط', "ṭāʾ", "طَاء", R.string.letter_sound_tta, 5),
        Letter("letter.zza", 'ظ', "ẓāʾ", "ظَاء", R.string.letter_sound_zza, 5),
        Letter("letter.ayn", 'ع', "ʿayn", "عَيۡن", R.string.letter_sound_ayn, 5),
        Letter("letter.ghayn", 'غ', "ghayn", "غَيۡن", R.string.letter_sound_ghayn, 5),
        // 6: ق is heavy; ل and م appear in almost every ayah
        Letter("letter.fa", 'ف', "fāʾ", "فَاء", R.string.letter_sound_fa, 6),
        Letter("letter.qaf", 'ق', "qāf", "قَاف", R.string.letter_sound_qaf, 6),
        Letter("letter.kaf", 'ك', "kāf", "كَاف", R.string.letter_sound_kaf, 6),
        Letter("letter.lam", 'ل', "lām", "لَام", R.string.letter_sound_lam, 6),
        Letter("letter.mim", 'م', "mīm", "مِيم", R.string.letter_sound_mim, 6),
        // 7: the last shapes; hamza's seats come in the Reading track
        Letter("letter.ha", 'ه', "hāʾ", "هَاء", R.string.letter_sound_ha, 7),
        Letter("letter.hamza", 'ء', "hamza", "هَمۡزَة", R.string.letter_sound_hamza, 7, joinsNext = false),
    )

    private val byId = all.associateBy { it.id }
    private val byChar = all.associateBy { it.char }

    operator fun get(id: String): Letter? = byId[id]

    fun ofChar(char: Char): Letter? = byChar[char]

    val groups: Map<Int, List<Letter>> = all.groupBy { it.group }

    /** The zero-width joiner: it makes a letter take its joined shape on its own. */
    private const val JOINER = '\u200D'

    /**
     * The four shapes, drawn by the font: a joiner on either side asks for the joined form.
     * Hamza on its own never joins; letters such as د join the one before but not after.
     */
    fun shapesOf(letter: Letter): LetterShapes {
        val c = letter.char.toString()
        return when {
            letter.id == "letter.hamza" -> LetterShapes(c, c, c, c)
            !letter.joinsNext -> LetterShapes(c, c, "$JOINER$c", "$JOINER$c")
            else -> LetterShapes(c, "$c$JOINER", "$JOINER$c$JOINER", "$JOINER$c")
        }
    }
}
