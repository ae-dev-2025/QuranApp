package com.quranapp.android.learning.letters

import androidx.annotation.StringRes
import com.quranapp.android.R
import com.quranapp.android.learning.analysis.Arabic
import com.quranapp.android.learning.concepts.ConceptIds

/**
 * One of the 28 letters, or hamza: the design's first kind of item. The [id] is stored in
 * the learner's progress, so it must never change once released.
 *
 * @property name The letter's name in Latin letters, as learners say it: "bāʾ".
 * @property arabicName Its name in Arabic, written for the app's Quran font (sukun is U+06E1).
 * @property latin How it's written in Latin letters in transliteration: "b", "th", "ʿ".
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
    val latin: String,
    @StringRes val soundRes: Int,
    /** Where it is made, exactly: "Both lips close, then open." */
    @StringRes val placeRes: Int,
    val place: Makhraj,
    val group: Int,
    val joinsNext: Boolean = true,
)

/** The five places letters are made in (makhārij), each a concept with its own lesson. */
enum class Makhraj(val conceptId: String) {
    JAWF(ConceptIds.MAKHRAJ_JAWF),
    THROAT(ConceptIds.MAKHRAJ_THROAT),
    TONGUE(ConceptIds.MAKHRAJ_TONGUE),
    LIPS(ConceptIds.MAKHRAJ_LIPS),
    NOSE(ConceptIds.MAKHRAJ_NOSE),
}

/** How a letter looks alone and at the start, middle and end of a word. */
data class LetterShapes(val alone: String, val start: String, val middle: String, val end: String)

/** The 29 letter items, in the order they are taught (decision 2's stage 0). */
object Letters {
    val all: List<Letter> = listOf(
        // 1: one base shape; the dots tell them apart
        Letter("letter.alif", 'ا', "alif", "أَلِف", "ā", R.string.letter_sound_alif, R.string.letter_place_alif, Makhraj.JAWF, 1, joinsNext = false),
        Letter("letter.ba", 'ب', "bāʾ", "بَاء", "b", R.string.letter_sound_ba, R.string.letter_place_ba, Makhraj.LIPS, 1),
        Letter("letter.ta", 'ت', "tāʾ", "تَاء", "t", R.string.letter_sound_ta, R.string.letter_place_ta, Makhraj.TONGUE, 1),
        Letter("letter.tha", 'ث', "thāʾ", "ثَاء", "th", R.string.letter_sound_tha, R.string.letter_place_tha, Makhraj.TONGUE, 1),
        Letter("letter.nun", 'ن', "nūn", "نُون", "n", R.string.letter_sound_nun, R.string.letter_place_nun, Makhraj.TONGUE, 1),
        Letter("letter.ya", 'ي', "yāʾ", "يَاء", "y", R.string.letter_sound_ya, R.string.letter_place_ya, Makhraj.TONGUE, 1),
        // 2: one shape; ح and خ come from the throat
        Letter("letter.jim", 'ج', "jīm", "جِيم", "j", R.string.letter_sound_jim, R.string.letter_place_jim, Makhraj.TONGUE, 2),
        Letter("letter.hha", 'ح', "ḥāʾ", "حَاء", "ḥ", R.string.letter_sound_hha, R.string.letter_place_hha, Makhraj.THROAT, 2),
        Letter("letter.kha", 'خ', "khāʾ", "خَاء", "kh", R.string.letter_sound_kha, R.string.letter_place_kha, Makhraj.THROAT, 2),
        // 3: never join the next letter
        Letter("letter.dal", 'د', "dāl", "دَال", "d", R.string.letter_sound_dal, R.string.letter_place_dal, Makhraj.TONGUE, 3, joinsNext = false),
        Letter("letter.dhal", 'ذ', "dhāl", "ذَال", "dh", R.string.letter_sound_dhal, R.string.letter_place_dhal, Makhraj.TONGUE, 3, joinsNext = false),
        Letter("letter.ra", 'ر', "rāʾ", "رَاء", "r", R.string.letter_sound_ra, R.string.letter_place_ra, Makhraj.TONGUE, 3, joinsNext = false),
        Letter("letter.zay", 'ز', "zāy", "زَاي", "z", R.string.letter_sound_zay, R.string.letter_place_zay, Makhraj.TONGUE, 3, joinsNext = false),
        Letter("letter.waw", 'و', "wāw", "وَاو", "w", R.string.letter_sound_waw, R.string.letter_place_waw, Makhraj.LIPS, 3, joinsNext = false),
        // 4: tooth shapes; ص and ض are heavy
        Letter("letter.sin", 'س', "sīn", "سِين", "s", R.string.letter_sound_sin, R.string.letter_place_sin, Makhraj.TONGUE, 4),
        Letter("letter.shin", 'ش', "shīn", "شِين", "sh", R.string.letter_sound_shin, R.string.letter_place_shin, Makhraj.TONGUE, 4),
        Letter("letter.sad", 'ص', "ṣād", "صَاد", "ṣ", R.string.letter_sound_sad, R.string.letter_place_sad, Makhraj.TONGUE, 4),
        Letter("letter.dad", 'ض', "ḍād", "ضَاد", "ḍ", R.string.letter_sound_dad, R.string.letter_place_dad, Makhraj.TONGUE, 4),
        // 5: ط and ظ are heavy; ع and غ come from the throat
        Letter("letter.tta", 'ط', "ṭāʾ", "طَاء", "ṭ", R.string.letter_sound_tta, R.string.letter_place_tta, Makhraj.TONGUE, 5),
        Letter("letter.zza", 'ظ', "ẓāʾ", "ظَاء", "ẓ", R.string.letter_sound_zza, R.string.letter_place_zza, Makhraj.TONGUE, 5),
        Letter("letter.ayn", 'ع', "ʿayn", "عَيۡن", "ʿ", R.string.letter_sound_ayn, R.string.letter_place_ayn, Makhraj.THROAT, 5),
        Letter("letter.ghayn", 'غ', "ghayn", "غَيۡن", "gh", R.string.letter_sound_ghayn, R.string.letter_place_ghayn, Makhraj.THROAT, 5),
        // 6: ق is heavy; ل and م appear in almost every ayah
        Letter("letter.fa", 'ف', "fāʾ", "فَاء", "f", R.string.letter_sound_fa, R.string.letter_place_fa, Makhraj.LIPS, 6),
        Letter("letter.qaf", 'ق', "qāf", "قَاف", "q", R.string.letter_sound_qaf, R.string.letter_place_qaf, Makhraj.TONGUE, 6),
        Letter("letter.kaf", 'ك', "kāf", "كَاف", "k", R.string.letter_sound_kaf, R.string.letter_place_kaf, Makhraj.TONGUE, 6),
        Letter("letter.lam", 'ل', "lām", "لَام", "l", R.string.letter_sound_lam, R.string.letter_place_lam, Makhraj.TONGUE, 6),
        Letter("letter.mim", 'م', "mīm", "مِيم", "m", R.string.letter_sound_mim, R.string.letter_place_mim, Makhraj.LIPS, 6),
        // 7: the last shapes; hamza's seats come in the Reading track
        Letter("letter.ha", 'ه', "hāʾ", "هَاء", "h", R.string.letter_sound_ha, R.string.letter_place_ha, Makhraj.THROAT, 7),
        Letter("letter.hamza", 'ء', "hamza", "هَمۡزَة", "ʾ", R.string.letter_sound_hamza, R.string.letter_place_hamza, Makhraj.THROAT, 7, joinsNext = false),
    )

    private val byId = all.associateBy { it.id }
    private val byChar = all.associateBy { it.char }

    operator fun get(id: String): Letter? = byId[id]

    fun ofChar(char: Char): Letter? = byChar[char]

    val groups: Map<Int, List<Letter>> = all.groupBy { it.group }

    /**
     * The letter on each short vowel and with sukun, with its transliteration: بَ ba, بِ bi,
     * بُ bu, بۡ b. Alif has none: it carries no vowel of its own.
     */
    fun syllablesOf(letter: Letter): List<Pair<String, String>> {
        if (letter.id == "letter.alif") return emptyList()
        val c = letter.char
        val l = letter.latin
        return listOf("$c${Arabic.FATHA}" to "${l}a", "$c${Arabic.KASRA}" to "${l}i", "$c${Arabic.DAMMA}" to "${l}u", "$c${Arabic.SUKUN}" to l)
    }

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
