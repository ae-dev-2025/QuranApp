package com.quranapp.android.learning.analysis

import com.quranapp.android.learning.concepts.GrammarIds

/**
 * One piece of a word as MASAQ analyses its sentence (from the learning pack): its part of
 * speech in MASAQ's own tags, and its role (iʿrāb).
 */
data class SyntaxSegment(
    /** `Prefix`, `Stem` or `Suffix`. */
    val morphType: String,
    /** MASAQ's part of speech, such as `NOUN_PROP`, `ADJ_COMP` (comparative) or `NOUN_TIME_PLACE`. */
    val morphTag: String,
    /** Such as `SUBJ`, `PRED`, `AGNT` (doer), `OBJ`, `GEN_CONS` (second noun of an iḍāfa) or `CIRCUM` (ḥāl). */
    val role: String?,
    /** `CONSTRUCT` on the first noun of an iḍāfa. */
    val construct: String? = null,
    /** The ending that shows the case, such as `DHAMMA` or `FATHA_DIPTOTE`. */
    val caseMarker: String? = null,
    /** `PHRASE`, `VERB_SNT` or `NOM_SNT` on the first word of a phrase or sentence that has a role. */
    val phrase: String? = null,
    /** That phrase's role, such as `PRED`. */
    val phraseFunction: String? = null,
)

/**
 * Finds the grammar concepts that show in how words relate in their sentence (milestone 9):
 * iḍāfa, subject and predicate, the verb's doer and object, ḥāl, tamyīz and the other
 * objects, from MASAQ's iʿrāb. The corpus's analysis of each word ([GrammarSegment]) adds
 * what MASAQ doesn't mark: a verb's person, for agreement and the hidden doer.
 */
object SentenceDetector {
    private val PREDICATE_PHRASES = setOf("PHRASE", "VERB_SNT", "NOM_SNT")
    private val PREDICATE_FUNCTIONS = setOf("PRED", "PART_COP_PRED", "V_COP_PRED", "NEG_CAT_PRED")
    private val PERSON = Regex("[123][MF]?[SDP]")
    private val MANY = setOf("MP", "FP", "P", "MD", "FD")

    /**
     * For each concept, the 0-based indexes of the words it is found in. [syntax] and [forms]
     * are the same words, by index; either may be empty for a word the sources don't cover.
     */
    fun detect(syntax: List<List<SyntaxSegment>>, forms: List<List<GrammarSegment>>): Map<String, List<Int>> {
        val found = LinkedHashMap<String, MutableList<Int>>()
        fun add(id: String, word: Int) {
            val list = found.getOrPut(id) { mutableListOf() }
            if (word !in list) list += word
        }
        val count = maxOf(syntax.size, forms.size)
        fun syntaxOf(word: Int) = syntax.getOrNull(word).orEmpty()
        fun stems(word: Int) = syntaxOf(word).filter { it.morphType == "Stem" }
        fun rolesOf(word: Int) = stems(word).mapNotNullTo(HashSet()) { it.role }
        fun isPredicate(word: Int) = "PRED" in rolesOf(word) || syntaxOf(word).any { it.phraseFunction == "PRED" }

        for (word in 0 until count) {
            val stems = stems(word)
            val roles = rolesOf(word)

            // رَبِّ ٱلۡعَٰلَمِينَ: a noun in construct, then the noun it belongs to. (A pronoun
            // owner, as in رَبُّكَ, is part of the same word: that is PRONOUN_POSSESSOR.)
            if (stems.any { it.construct == "CONSTRUCT" } && stems(word + 1).any { it.role == "GEN_CONS" }) {
                add(GrammarIds.IDAFA, word)
                add(GrammarIds.IDAFA, word + 1)
            }
            if ("SUBJ" in roles || isPredicate(word)) add(GrammarIds.MUBTADA_KHABAR, word)
            if (syntaxOf(word).any { it.phrase in PREDICATE_PHRASES && it.phraseFunction in PREDICATE_FUNCTIONS }) {
                add(GrammarIds.KHABAR_PHRASE, word)
            }
            // A subject placed after its predicate: لَهُمۡ عَذَابٌ. Only with the predicate
            // really before it; MASAQ marks a few that have none.
            if ("SUBJ_DELA" in roles) {
                (word - 1 downTo 0).firstOrNull { isPredicate(it) }?.let { predicate ->
                    add(GrammarIds.KHABAR_FIRST, predicate)
                    add(GrammarIds.KHABAR_FIRST, word)
                }
            }
            if ("AGNT" in roles || "OBJ" in roles) add(GrammarIds.VERB_DOER_OBJECT, word)
            if ("COGN" in roles || "SUBS_COG_ACC" in roles) add(GrammarIds.ABSOLUTE_OBJECT, word)
            if ("PURP" in roles) add(GrammarIds.OBJECT_OF_REASON, word)
            if ("ADV_TIME" in roles || "ADV_PLCE" in roles) add(GrammarIds.TIME_PLACE, word)
            if ("CIRCUM" in roles || syntaxOf(word).any { it.phraseFunction == "CIRCUM" }) add(GrammarIds.HAL, word)
            if ("ACC_SPECIF" in roles) add(GrammarIds.TAMYIZ, word)
            if (stems.any { it.caseMarker == "FATHA_DIPTOTE" }) add(GrammarIds.DIPTOTE, word)
            if (stems.any { it.morphTag == "ADJ_COMP" }) add(GrammarIds.ELATIVE, word)
            if (stems.any { it.morphTag == "NOUN_TIME_PLACE" || it.morphTag == "ADJ_INTENS" }) {
                add(GrammarIds.PLACE_TIME_INTENSIVE, word)
            }
            if ("AGNT" in roles) agreement(word, forms)?.let { verb ->
                add(GrammarIds.VERB_AGREEMENT, verb)
                add(GrammarIds.VERB_AGREEMENT, word)
            }
        }

        for (word in forms.indices) {
            if (hasHiddenDoer(word, forms) { next -> rolesOf(next).any { it == "AGNT" || it == "SUBJ_COP_V" } }) {
                add(GrammarIds.HIDDEN_DOER, word)
            }
        }
        return found
    }

    private fun verbOf(word: List<GrammarSegment>?) = word?.firstOrNull { it.kind == "STEM" && it.tag == "V" }

    private fun personOf(verb: GrammarSegment) = verb.features.firstOrNull { PERSON.matches(it) }

    /**
     * The verb before a noun doer, when the pair shows agreement worth seeing: a feminine verb
     * (قَالَتِ), or a singular verb before a dual or plural doer (جَآءَ رُسُلُنَا).
     */
    private fun agreement(doer: Int, forms: List<List<GrammarSegment>>): Int? {
        val noun = forms.getOrNull(doer)?.firstOrNull { it.kind == "STEM" && (it.tag == "N" || it.tag == "PN" || it.tag == "ADJ") }
            ?: return null
        val verbWord = (doer - 1 downTo 0).firstOrNull { verbOf(forms[it]) != null } ?: return null
        val person = personOf(verbOf(forms[verbWord])!!)
        val many = noun.features.any { it in MANY }
        return verbWord.takeIf { person == "3FS" || (many && person == "3MS") }
    }

    /**
     * A verb whose doer isn't written: always for "I", "we" and one "you" in the present and
     * the command (نَعۡبُدُ, قُلۡ); for "he" and "she" when no doer follows before the next
     * verb (خَلَقَ … ). A doer ending, as in قَالُوا, is written, so it isn't hidden.
     */
    private fun hasHiddenDoer(word: Int, forms: List<List<GrammarSegment>>, isDoer: (Int) -> Boolean): Boolean {
        val verb = verbOf(forms[word]) ?: return false
        if ("PASS" in verb.features) return false
        val person = personOf(verb)
        return when {
            "IMPV" in verb.features -> person == "2MS"
            "IMPF" in verb.features && person in setOf("1S", "1P", "2MS") -> true
            person == "3MS" || person == "3FS" -> {
                val next = (word + 1 until forms.size).takeWhile { verbOf(forms[it]) == null }
                next.none(isDoer)
            }
            else -> false
        }
    }
}
