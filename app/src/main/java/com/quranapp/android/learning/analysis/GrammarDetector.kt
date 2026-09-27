package com.quranapp.android.learning.analysis

import com.quranapp.android.learning.concepts.GrammarIds

/**
 * One piece of a word as the Quranic Arabic Corpus analyses it (from the learning pack):
 * `kind` is PREFIX, STEM or SUFFIX; `tag` the part of speech (N, V, P, PRON…); `features`
 * the corpus's features (MP, GEN, PERF, (X), MOOD:JUS, l:EMPH+…).
 */
data class GrammarSegment(
    val kind: String,
    val tag: String,
    val features: List<String>,
    val form: String,
    val lemmaKey: String? = null,
    /** The root in Buckwalter letters, hamza written as A: `qwl`, `Ax*`. */
    val rootKey: String? = null,
)

/**
 * Finds the grammar concepts that show in the form of each word (milestone 8): word types,
 * gender and number, cases, pronouns, tenses and moods, verb forms, participles, particles.
 * Concepts about how words relate (iḍāfa, subject and predicate, objects) need the sentence
 * roles and come in milestone 9.
 */
object GrammarDetector {
    private val NOUN_TAGS = setOf("N", "PN", "ADJ", "PRON", "DEM", "REL", "T", "LOC", "IMPN")
    private val FIVE_NOUNS = setOf(">abN", ">ax", "*uw")
    private val FIVE_NOUN_ENDINGS = setOf('و', Arabic.ALIF, Arabic.YA, Arabic.ALIF_MAQSURA)
    private val IN_AN = setOf("<in~", "<in", ">an", ">an~")
    private val LAW = setOf("law", "lawolaA^")
    private val FORMS_2_4 = setOf("(II)", "(III)", "(IV)")
    private val FORMS_5_6 = setOf("(V)", "(VI)")
    private val FORMS_7_10 = setOf("(VII)", "(VIII)", "(IX)", "(X)", "(XI)", "(XII)")
    private val LAM_PARTICLES = setOf("l:EMPH+", "l:PRP+", "l:IMPV+")

    /** For each concept, the 0-based indexes of the words it is found in. */
    fun detect(words: List<List<GrammarSegment>>): Map<String, List<Int>> {
        val found = LinkedHashMap<String, MutableList<Int>>()
        fun add(id: String, word: Int) {
            val list = found.getOrPut(id) { mutableListOf() }
            if (word !in list) list += word
        }
        words.forEachIndexed { index, segments ->
            wordConcepts(segments).forEach { add(it, index) }
            // لَا denying a whole kind: لَا, then a noun in naṣb with neither ال nor tanwīn (لَا رَيۡبَ).
            if (segments.any { it.kind == "STEM" && it.lemmaKey == "laA" && it.tag == "NEG" }) {
                val next = words.getOrNull(index + 1)
                val noun = next?.firstOrNull { it.kind == "STEM" && it.tag == "N" }
                if (noun != null && "ACC" in noun.features && "INDEF" !in noun.features && next.none { it.tag == "DET" }) {
                    add(GrammarIds.LA_GENERIC, index)
                    add(GrammarIds.LA_GENERIC, index + 1)
                }
            }
        }
        return found
    }

    private fun wordConcepts(segments: List<GrammarSegment>): Set<String> {
        val found = mutableSetOf<String>()
        val stems = segments.filter { it.kind == "STEM" }
        val features = segments.flatMap { it.features }.toSet()

        for (s in stems) {
            val f = s.features.toSet()
            when {
                s.tag in NOUN_TAGS -> found += GrammarIds.ISM
                s.tag == "V" -> found += GrammarIds.FIL
                else -> found += GrammarIds.HARF
            }
            // Proper nouns have cases too: ٱللَّهُ, ٱللَّهَ, ٱللَّهِ.
            if (s.tag == "N" || s.tag == "ADJ" || s.tag == "PN") nounConcepts(s, f, found)
            if (s.tag == "V") verbConcepts(s, f, segments, found)
            if ("PCPL" in f && "ACT" in f) found += GrammarIds.ACTIVE_PARTICIPLE
            if ("PCPL" in f && "PASS" in f) found += GrammarIds.PASSIVE_PARTICIPLE
            if ("VN" in f) found += GrammarIds.VERBAL_NOUN
            if (s.tag == "N" || s.tag == "ADJ" || s.tag == "V") {
                if (f.any { it in FORMS_2_4 }) found += GrammarIds.FORMS_2_4
                if (f.any { it in FORMS_5_6 }) found += GrammarIds.FORMS_5_6
                if (f.any { it in FORMS_7_10 }) found += GrammarIds.FORMS_7_10
                // Every word with a root is built on a pattern: the root and its measure on فَعَلَ.
                if (s.rootKey != null) found += setOf(GrammarIds.ROOT, GrammarIds.PATTERN)
            }
            when (s.tag) {
                "PRON" -> found += if (s.lemmaKey == "<iy~aA") GrammarIds.IYYA else GrammarIds.DETACHED_PRONOUN
                "DEM" -> found += GrammarIds.DEMONSTRATIVE
                "REL" -> found += GrammarIds.RELATIVE
                "ADJ" -> found += GrammarIds.ADJECTIVE
                "P" -> found += GrammarIds.PREPOSITION
                "ACC" -> found += GrammarIds.INNA
                "CERT", "FUT" -> found += GrammarIds.QAD_SA
                "PRO" -> found += GrammarIds.PROHIBITION
                "INTG" -> found += GrammarIds.QUESTIONS
                "EXP" -> found += GrammarIds.EXCEPTION
                "RES" -> found += GrammarIds.RESTRICTION
                "VOC" -> found += GrammarIds.VOCATIVE
                "CONJ" -> found += GrammarIds.WA_FA_THUMMA
                "EMPH" -> found += GrammarIds.EMPHASIS
                "COND" -> found += if (s.lemmaKey in LAW) GrammarIds.LAW else GrammarIds.CONDITIONS
            }
            when (s.lemmaKey) {
                "maA" -> found += GrammarIds.MA
                "laA" -> found += GrammarIds.LA
                "lawolaA^" -> found += GrammarIds.LAW
                in IN_AN -> found += GrammarIds.IN_AN
            }
        }

        // Prefixes and suffixes. A prefix particle (the بِ of بِسۡمِ) is a ḥarf too; the article isn't counted as one.
        if (segments.any { it.kind == "PREFIX" && it.tag != "DET" }) found += GrammarIds.HARF
        if (segments.any { it.kind == "PREFIX" && it.tag == "DET" } || "INDEF" in features) found += GrammarIds.DEFINITENESS
        if (segments.any { it.kind == "PREFIX" && it.tag == "P" }) found += GrammarIds.PREPOSITION
        if (segments.any { it.kind == "PREFIX" && it.tag == "CONJ" }) found += GrammarIds.WA_FA_THUMMA
        if ("A:INTG+" in features) found += GrammarIds.QUESTIONS
        if ("ya+" in features) found += GrammarIds.VOCATIVE
        if ("sa+" in features) found += GrammarIds.QAD_SA
        if (features.any { it in LAM_PARTICLES }) found += GrammarIds.LAM_PARTICLES
        if ("l:EMPH+" in features || "+n:EMPH" in features || "w:P+" in features) found += GrammarIds.EMPHASIS
        if (segments.any { it.kind == "SUFFIX" && it.tag == "PRON" }) {
            found += GrammarIds.ATTACHED_PRONOUN
            // A noun with an attached pronoun: the pronoun is its possessor (رَبُّكَ).
            if (stems.any { it.tag == "N" }) found += GrammarIds.PRONOUN_POSSESSOR
        }
        return found
    }

    private fun nounConcepts(s: GrammarSegment, f: Set<String>, found: MutableSet<String>) {
        if (f.any { it == "F" || it == "FS" || it == "FP" || it == "FD" }) found += GrammarIds.GENDER
        if (f.any { it == "NOM" || it == "ACC" || it == "GEN" }) found += GrammarIds.CASES
        val skeleton = skeleton(s.form)
        val dual = f.any { it == "MD" || it == "FD" }
        // A plural's gender isn't always given: أَفۡوَاجًا is just P.
        val plural = f.any { it == "MP" || it == "FP" || it == "P" }
        val soundMasculine = "MP" in f && (skeleton.endsWith("ون") || skeleton.endsWith("ين"))
        val soundFeminine = "FP" in f && skeleton.endsWith("ات")
        if (dual) found += GrammarIds.DUAL
        if (soundMasculine) found += GrammarIds.SOUND_MASC_PLURAL
        if (soundFeminine) found += GrammarIds.SOUND_FEM_PLURAL
        if (plural && !soundMasculine && !soundFeminine) found += GrammarIds.BROKEN_PLURAL
        if (dual || soundMasculine) found += GrammarIds.DUAL_PLURAL_ENDINGS
        // The five nouns show their case with a long vowel: أَبُو, أَخَا, ذِي (the pack writes أَبِى).
        if (s.lemmaKey in FIVE_NOUNS && skeleton.lastOrNull() in FIVE_NOUN_ENDINGS) found += GrammarIds.FIVE_NOUNS
    }

    private fun verbConcepts(s: GrammarSegment, f: Set<String>, segments: List<GrammarSegment>, found: MutableSet<String>) {
        when {
            "PERF" in f -> found += GrammarIds.PAST
            "IMPF" in f -> found += GrammarIds.PRESENT
            "IMPV" in f -> found += GrammarIds.COMMAND
        }
        if ("PASS" in f) found += GrammarIds.PASSIVE
        if ("MOOD:SUBJ" in f) found += GrammarIds.SUBJUNCTIVE
        if ("MOOD:JUS" in f) found += GrammarIds.JUSSIVE
        if ("SP:kaAn" in f) found += GrammarIds.KANA
        // The doer inside the verb: a suffix pronoun of the verb's own person (أَنۡعَمۡتَ); a
        // suffix of another person is its object (ٱهۡدِنَا).
        val person = f.firstOrNull { it.matches(Regex("[123][MF]?[SDP]")) }
        if (person != null && segments.any { it.kind == "SUFFIX" && it.tag == "PRON" && "PRON:$person" in it.features }) {
            found += GrammarIds.DOER_IN_VERB
        }
        s.rootKey?.takeIf { it.length == 3 }?.let { root ->
            if (root[1] == 'w' || root[1] == 'y') found += GrammarIds.HOLLOW
            if (root[2] == 'w' || root[2] == 'y') found += GrammarIds.DEFECTIVE
            if (root[0] == 'w' || root[1] == root[2] || 'A' in root) found += GrammarIds.OTHER_WEAK
        }
    }

    /** The letters of a form. A dagger alif is the long ā it stands for, so ٱلصَّٰلِحَٰتِ ends in ات. */
    private fun skeleton(form: String): String =
        form.replace(Arabic.DAGGER_ALIF, Arabic.ALIF).filter { Arabic.isLetter(it) }.replace(Arabic.ALIF_WASLA, Arabic.ALIF)
}
