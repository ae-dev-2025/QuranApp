package com.quranapp.android.learning.analysis

import com.quranapp.android.learning.concepts.ConceptIds

/**
 * A concept found in an ayah, at the word with this 0-based index. A rule that joins two
 * words (مِّن رَّبِّكَ) is reported once for each of them.
 */
data class Occurrence(val conceptId: String, val wordIndex: Int)

/**
 * Finds the Tajweed-track concepts in one ayah.
 *
 * Many rules depend on the *next* letter, which may be in the next word, so this works on
 * the whole ayah at once. Rules are read from the marks of the Madinah muṣḥaf, which already
 * tell the reader what to do (for example, a noon with a sukun is always pronounced clearly).
 *
 * At the end of the ayah we assume the reader stops, so rules that join two letters across
 * that boundary are not reported.
 */
object TajweedDetector {
    private val HEAVY_LETTERS = setOf('خ', 'ص', 'ض', 'غ', 'ط', 'ق', 'ظ')

    /** Letters that only carry a long vowel or a hamza's seat, never merging into the next letter. */
    private val NOT_MERGING = setOf(Arabic.ALIF, Arabic.ALIF_MAQSURA, Arabic.TATWEEL, Arabic.ALIF_WASLA)
    private val QALQALAH_LETTERS = setOf('ق', 'ط', 'ب', 'ج', 'د')
    private val IDGHAM_GHUNNAH_LETTERS = setOf('ي', 'ن', 'م', 'و')
    private val IDGHAM_NO_GHUNNAH_LETTERS = setOf('ل', 'ر')
    private val IKHFA_LETTERS =
        setOf('ت', 'ث', 'ج', 'د', 'ذ', 'ز', 'س', 'ش', 'ص', 'ض', 'ط', 'ظ', 'ف', 'ق', 'ك')

    /** Marks that give a letter an open vowel: fatḥa or ḍamma, and their tanwīn. */
    private val OPEN_VOWELS = setOf(
        Arabic.FATHA, Arabic.DAMMA, Arabic.FATHATAN, Arabic.DAMMATAN, Arabic.OPEN_FATHATAN, Arabic.OPEN_DAMMATAN,
        Arabic.DAGGER_ALIF,
    )
    private val KASRAS = setOf(Arabic.KASRA, Arabic.KASRATAN, Arabic.OPEN_KASRATAN)
    private val FATHATAN = setOf(Arabic.FATHATAN, Arabic.OPEN_FATHATAN)

    /** Pairs made in the same place (mutajānisayn), first letter then second, as the muṣḥaf merges them. */
    private val SAME_PLACE = setOf("دت", "تط", "تد", "ذظ", "ثذ", "بم", "طت")

    /** Pairs made in close places (mutaqāribayn). */
    private val CLOSE_PLACES = setOf("لر", "قك")

    /** Words where a yāʾ was dropped after the rāʾ: stopping on them, it may be heavy or light. */
    private val RA_EITHER_WHEN_STOPPING = setOf("ونذر", "يسر")

    /** A letter plus where it sits in the ayah. */
    private data class Letter(
        val cluster: LetterCluster,
        val wordIndex: Int,
        val indexInWord: Int,
        val isLastInWord: Boolean,
    )

    /**
     * A rule found at a letter. [joinsNextLetter] is true for rules about how this letter meets
     * the next one (noon and meem sākinah, madd before a hamza).
     */
    private data class Rule(val conceptId: String, val joinsNextLetter: Boolean = false)

    /** @param words the ayah's words, each already split into letters with [parseClusters]. */
    fun detect(words: List<List<LetterCluster>>): List<Occurrence> {
        val letters = words.flatMapIndexed { wordIndex, clusters ->
            clusters.mapIndexed { i, cluster ->
                Letter(cluster, wordIndex, i, isLastInWord = i == clusters.lastIndex)
            }
        }

        val found = mutableListOf<Occurrence>()

        letters.forEachIndexed { index, letter ->
            val next = nextSoundedLetter(letters, index)
            val isLastOfAyah = index == letters.lastIndex

            for (rule in rulesAt(letters, index, next, isLastOfAyah)) {
                found += Occurrence(rule.conceptId, letter.wordIndex)

                // The letter that completes the rule may start the next word: report that word
                // too, so the learner sees where the two words meet.
                if (rule.joinsNextLetter && next != null && next.wordIndex != letter.wordIndex) {
                    found += Occurrence(rule.conceptId, next.wordIndex)
                }
            }
        }

        return found
    }

    private fun rulesAt(
        letters: List<Letter>,
        index: Int,
        next: Letter?,
        isLastOfAyah: Boolean,
    ): List<Rule> {
        val letter = letters[index]
        val c = letter.cluster
        val rules = mutableListOf<Rule>()

        if (c.letter in HEAVY_LETTERS) rules += Rule(ConceptIds.HEAVY_LETTERS)

        if ((c.letter == Arabic.NOON || c.letter == Arabic.MEEM) && c.has(Arabic.SHADDA)) {
            rules += Rule(ConceptIds.GHUNNAH)
        }

        // Qalqalah: the letter has a sukun, or it is the last letter and we stop on it.
        // Not in a letter's name, such as the ق of عٓسٓقٓ, read qāf.
        if (c.letter in QALQALAH_LETTERS && !c.hasAny(Arabic.MADD_SIGNS) && (c.has(Arabic.SUKUN) || isLastOfAyah)) {
            rules += Rule(ConceptIds.QALQALAH)
        }

        noonSakinahRule(c, next)?.let { rules += Rule(it, joinsNextLetter = true) }
        meemSakinahRule(c, next)?.let { rules += Rule(it, joinsNextLetter = true) }
        definiteArticleRule(letters, index)?.let { rules += Rule(it) }
        if (isLamOfAllah(letters, index)) rules += Rule(ConceptIds.LAM_OF_ALLAH)

        if (c.hasAny(Arabic.MADD_SIGNS)) {
            rules += Rule(ConceptIds.MADD_SIGN)
            maddRule(letter, next)?.let { rules += Rule(it, joinsNextLetter = true) }
        }

        raRule(letters, index, stopped = isLastOfAyah)?.let { rules += Rule(it) }
        twoLetterIdgham(letters, index, next)?.let { rules += Rule(it, joinsNextLetter = true) }
        if (isLamSakinah(letters, index, next)) rules += Rule(ConceptIds.LAM_SAKINAH, joinsNextLetter = c.isBare)
        if (c.hasAny(Arabic.TANWEEN) && next?.cluster?.letter == Arabic.ALIF_WASLA) {
            rules += Rule(ConceptIds.TANWEEN_BEFORE_WASL, joinsNextLetter = true)
        }

        return rules
    }

    /**
     * Heavy or light rāʾ, for Ḥafṣ. Its own vowel decides: fatḥa or ḍamma heavy, kasra light.
     * With a sukūn, or when we stop on it, the vowel before it decides the same way; after the
     * ٱ of hamzat al-waṣl it is heavy (ٱرۡكَعُواْ), after a yāʾ light (خَيۡر, قَدِير), and after
     * a kasra a heavy letter right after it keeps it heavy (قِرۡطَاس).
     *
     * Null where either is allowed (فِرۡقٖ; stopping on مِصۡرَ, وَنُذُرِ, يَسۡرِ), for a rāʾ merged
     * into the next one (وَٱذۡكُر رَّبَّكَ) and for one read as a letter name (الٓرۚ).
     */
    private fun raRule(letters: List<Letter>, index: Int, stopped: Boolean): String? {
        val letter = letters[index]
        val c = letter.cluster
        if (c.letter != Arabic.RA || c.hasAny(Arabic.SILENT_MARKS)) return null
        if (c.hasAny(FATHATAN)) return ConceptIds.RA_HEAVY // stopping on -ran gives -rā
        if (!stopped) {
            when {
                c.hasAny(OPEN_VOWELS) -> return ConceptIds.RA_HEAVY
                c.hasAny(KASRAS) -> return ConceptIds.RA_LIGHT
                !c.has(Arabic.SUKUN) -> return null
            }
        }

        // A rāʾ with a sukūn, or one we stop on: the letters before it in its word decide.
        val start = index - letter.indexInWord
        val word = letters.subList(start, letters.size).takeWhile { it.wordIndex == letter.wordIndex }.map { it.cluster }
        val at = letter.indexInWord
        if (at == 0) return null
        if (stopped && c.has(Arabic.KASRA) && word.joinToString("") { it.letter.toString() } in RA_EITHER_WHEN_STOPPING) return null

        val previous = word[at - 1]
        if (previous.letter == Arabic.ALIF_WASLA) return ConceptIds.RA_HEAVY
        if (stopped && previous.letter == Arabic.YA && (previous.has(Arabic.SUKUN) || previous.isBare)) return ConceptIds.RA_LIGHT

        // The nearest letter before it with a vowel; the ones skipped have a sukūn or carry a long vowel.
        var j = at - 1
        while (j >= 0 && !word[j].hasAny(OPEN_VOWELS) && !word[j].hasAny(KASRAS)) {
            if (word[j].letter == Arabic.ALIF_WASLA) return ConceptIds.RA_HEAVY
            j--
        }
        if (j < 0) return null
        if (word[j].hasAny(OPEN_VOWELS)) return ConceptIds.RA_HEAVY

        // After a kasra it is light, unless a heavy letter changes it: one with a sukūn in between
        // when stopping (مِصۡر, ٱلۡقِطۡر) allows both; one right after it keeps it heavy (قِرۡطَاس),
        // or allows both if that letter has a kasra (فِرۡقٖ).
        if (stopped && (j + 1 until at).any { word[it].letter in HEAVY_LETTERS && word[it].has(Arabic.SUKUN) }) return null
        val after = word.getOrNull(at + 1)
        if (!stopped && after != null && after.letter in HEAVY_LETTERS) {
            return if (after.hasAny(KASRAS)) null else ConceptIds.RA_HEAVY
        }
        return ConceptIds.RA_LIGHT
    }

    /**
     * Two letters that meet with the first one silent. The muṣḥaf shows the merge: no mark on
     * the first letter and a shadda on the second (قَد تَّبَيَّنَ). ط before ت is merged only
     * partly, keeping its heaviness, so the ت has no shadda (بَسَطتَ). Noon and meem have
     * rules of their own, and a long vowel is not a letter that merges.
     */
    private fun twoLetterIdgham(letters: List<Letter>, index: Int, next: Letter?): String? {
        val letter = letters[index]
        val c = letter.cluster
        if (next == null || !c.isBare || c.hasAny(Arabic.MADD_SIGNS) || c.hasAny(Arabic.SILENT_MARKS)) return null
        if (c.letter == Arabic.NOON || c.letter == Arabic.MEEM || c.letter in NOT_MERGING || isArticleLam(letters, index)) return null
        // A wāw or yāʾ after a fatḥa is a consonant, and merges into the same letter starting the next word (عَصَواْ وَّكَانُواْ).
        if (c.letter == Arabic.WAW || c.letter == Arabic.YA) {
            val afterFatha = letter.indexInWord > 0 && letters[index - 1].cluster.has(Arabic.FATHA)
            if (!afterFatha || next.wordIndex == letter.wordIndex) return null
        }
        val pair = "${c.letter}${next.cluster.letter}"
        val merged = next.cluster.has(Arabic.SHADDA)
        return when {
            merged && c.letter == next.cluster.letter -> ConceptIds.IDGHAM_MITHLAYN
            merged && pair in SAME_PLACE -> ConceptIds.IDGHAM_MUTAJANISAYN
            merged && pair in CLOSE_PLACES -> ConceptIds.IDGHAM_MUTAQARIBAYN
            pair == "طت" && next.wordIndex == letter.wordIndex -> ConceptIds.IDGHAM_MUTAJANISAYN
            else -> null
        }
    }

    /**
     * A lām with sukūn that isn't the ال of a noun (قُلۡ, هَلۡ, جَعَلۡنَا, عِلۡم): always pronounced,
     * except before ل or ر, where the muṣḥaf shows it merged (قُل رَّبِّ).
     */
    private fun isLamSakinah(letters: List<Letter>, index: Int, next: Letter?): Boolean {
        val c = letters[index].cluster
        if (c.letter != Arabic.LAM || c.hasAny(Arabic.MADD_SIGNS) || isArticleLam(letters, index)) return false
        if (c.has(Arabic.SUKUN)) return true
        return c.isBare && next != null && next.cluster.has(Arabic.SHADDA) &&
            (next.cluster.letter == Arabic.LAM || next.cluster.letter == Arabic.RA)
    }

    /** The lām of ال: after ٱ, after the لِ of لِلۡ, or after the آ of a question (ءَآلذَّكَرَيۡنِ). */
    private fun isArticleLam(letters: List<Letter>, index: Int): Boolean {
        val letter = letters[index]
        if (letter.cluster.letter != Arabic.LAM || letter.indexInWord == 0) return false
        val previous = letters[index - 1]
        return previous.cluster.letter == Arabic.ALIF_WASLA ||
            (previous.cluster.letter == Arabic.LAM && previous.cluster.has(Arabic.KASRA) && previous.indexInWord <= 1) ||
            (previous.cluster.letter == Arabic.ALIF && letter.indexInWord <= 2)
    }

    /** Noon sakinah and tanween: iẓhār, idghām, iqlāb or ikhfāʾ. */
    private fun noonSakinahRule(c: LetterCluster, next: Letter?): String? {
        // A noon with a madd sign is a letter name (نٓ in 68:1), not a noon sakinah.
        val isNoonSakinah = c.letter == Arabic.NOON &&
            !c.hasAny(Arabic.MADD_SIGNS) &&
            (c.isBare || c.has(Arabic.SUKUN))
        // Before ب, tanween is written as one vowel plus a small meem (مُحِيطُۢ, عَوَانُۢ).
        val isTanween = c.hasAny(Arabic.TANWEEN) ||
            (c.hasAny(Arabic.SHORT_VOWELS) && c.hasAny(Arabic.IQLAB_MARKS))

        if (!isNoonSakinah && !isTanween) return null
        if (next == null) return null // we stop at the end of the ayah
        if (next.cluster.letter == Arabic.ALIF_WASLA) return null // tanween gets a helping vowel

        // The mark itself tells the rule...
        if (c.hasAny(Arabic.IQLAB_MARKS)) return ConceptIds.IQLAB
        if (c.has(Arabic.SUKUN) || c.hasAny(Arabic.STACKED_TANWEEN)) return ConceptIds.IZHAR

        // ...otherwise (bare noon, staggered tanween) the next letter decides.
        return when (next.cluster.letter) {
            in IDGHAM_GHUNNAH_LETTERS -> ConceptIds.IDGHAM_GHUNNAH
            in IDGHAM_NO_GHUNNAH_LETTERS -> ConceptIds.IDGHAM_NO_GHUNNAH
            in IKHFA_LETTERS -> ConceptIds.IKHFA
            else -> null
        }
    }

    /** Meem sakinah: iẓhār, idghām or ikhfāʾ shafawī. */
    private fun meemSakinahRule(c: LetterCluster, next: Letter?): String? {
        if (c.letter != Arabic.MEEM || next == null || c.hasAny(Arabic.MADD_SIGNS)) return null

        if (c.has(Arabic.SUKUN)) return ConceptIds.IZHAR_SHAFAWI

        if (c.isBare) {
            return when (next.cluster.letter) {
                Arabic.BA -> ConceptIds.IKHFA_SHAFAWI
                Arabic.MEEM -> ConceptIds.IDGHAM_SHAFAWI
                else -> null
            }
        }

        return null
    }

    /**
     * The lām of the definite article ال: silent before a sun letter (the next letter gets a
     * shadda), pronounced with sukun before a moon letter.
     *
     * Known limitation: a verb such as ٱلۡتَقَى (iltaqā) also starts with ٱلۡ and is reported
     * as lām qamariyya. The pronunciation is the same, only the name is wrong.
     */
    private fun definiteArticleRule(letters: List<Letter>, index: Int): String? {
        val letter = letters[index]
        val c = letter.cluster
        if (c.letter != Arabic.LAM || letter.indexInWord == 0) return null

        val previous = letters[index - 1].cluster
        val afterAlifWasla = previous.letter == Arabic.ALIF_WASLA
        // لِ + ال drops the alif: لِلنَّاسِ, لِلۡمُتَّقِينَ
        val afterPrepositionLi = previous.letter == Arabic.LAM &&
            previous.has(Arabic.KASRA) &&
            letters[index - 1].indexInWord <= 1

        if (!afterAlifWasla && !afterPrepositionLi) return null

        val following = letters.getOrNull(index + 1)?.takeIf { it.wordIndex == letter.wordIndex }

        return when {
            c.has(Arabic.SUKUN) -> ConceptIds.LAM_QAMARIYYA
            c.isBare && following?.cluster?.has(Arabic.SHADDA) == true -> ConceptIds.LAM_SHAMSIYYA
            else -> null
        }
    }

    /**
     * The name Allāh: ل followed by لّ and a voweled ه that ends the word (ٱللَّهِ, لِلَّهِ),
     * or is followed only by the م of ٱللَّهُمَّ. This rules out look-alikes such as
     * ٱللَّهۡوِ (al-lahw, "amusement") and ٱللَّهَبِ (al-lahab, "the flame").
     * Reported once, at the first lām.
     */
    private fun isLamOfAllah(letters: List<Letter>, index: Int): Boolean {
        val first = letters[index]
        val second = letters.getOrNull(index + 1) ?: return false
        val ha = letters.getOrNull(index + 2) ?: return false
        val afterHa = letters.getOrNull(index + 3)?.takeIf { it.wordIndex == ha.wordIndex }

        return first.cluster.letter == Arabic.LAM &&
            second.cluster.letter == Arabic.LAM &&
            second.cluster.has(Arabic.SHADDA) &&
            ha.cluster.letter == Arabic.HA &&
            !ha.cluster.has(Arabic.SUKUN) &&
            first.wordIndex == ha.wordIndex &&
            (afterHa == null || afterHa.cluster.letter == Arabic.MEEM)
    }

    /** Classifies a madd sign as muttaṣil, munfaṣil or lāzim when the text makes it clear. */
    private fun maddRule(letter: Letter, next: Letter?): String? {
        if (next == null) return null

        val nextIsHamza = next.cluster.letter in Arabic.HAMZA_LETTERS ||
            next.cluster.has(Arabic.HAMZA_ABOVE)
        val sameWord = next.wordIndex == letter.wordIndex

        return when {
            // يَٰٓأَيُّهَا, هَٰٓؤُلَآءِ: the vocative yā / attention hā are written joined to the next
            // word, but count as a separate word, so the madd is munfaṣil.
            isJoinedVocative(letter) && nextIsHamza -> ConceptIds.MADD_MUNFASIL
            sameWord && nextIsHamza -> ConceptIds.MADD_MUTTASIL
            sameWord && (next.cluster.has(Arabic.SHADDA) || next.cluster.has(Arabic.SUKUN)) ->
                ConceptIds.MADD_LAZIM
            !sameWord && nextIsHamza -> ConceptIds.MADD_MUNFASIL
            else -> null
        }
    }

    private fun isJoinedVocative(letter: Letter): Boolean {
        val c = letter.cluster
        return (c.letter == Arabic.YA || c.letter == Arabic.HA) &&
            c.has(Arabic.DAGGER_ALIF) &&
            letter.indexInWord <= 1
    }

    /**
     * The next letter that is actually pronounced as a consonant. Skips letters marked
     * silent, and the alif/alif maqsūra/tatweel that only carry a long vowel or tanween
     * (the ا in مَثَلٗا, the ى in هُدٗى).
     */
    private fun nextSoundedLetter(letters: List<Letter>, from: Int): Letter? {
        for (j in from + 1 until letters.size) {
            val candidate = letters[j]
            val c = candidate.cluster

            val isSilent = c.hasAny(Arabic.SILENT_MARKS)
            val isVowelSeat = candidate.indexInWord > 0 &&
                c.isBare &&
                !c.has(Arabic.HAMZA_ABOVE) &&
                (c.letter == Arabic.ALIF || c.letter == Arabic.ALIF_MAQSURA || c.letter == Arabic.TATWEEL)

            if (!isSilent && !isVowelSeat) return candidate
        }
        return null
    }
}
