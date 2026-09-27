package com.quranapp.android.learning.path

import com.quranapp.android.learning.analysis.AyahAnalyzer
import com.quranapp.android.learning.concepts.Concept
import com.quranapp.android.learning.concepts.ConceptGraph

/**
 * What a surah needs, the same way the Understand sheet counts an ayah: the concepts its
 * text needs, prerequisites included, and its words.
 */
data class SurahNeeds(
    val surahNo: Int,
    /** In learning order. */
    val concepts: List<Concept>,
    /**
     * Every word of the surah as the item ids of its dictionary words (وَمِمَّا needs two),
     * repeats included; words without a dictionary word are left out. Null without the pack.
     */
    val words: List<List<String>>?,
) {
    companion object {
        /** [ayahs] are the Uthmani words of each ayah, as the app stores them. */
        fun conceptsOf(ayahs: List<List<String>>, graph: ConceptGraph = ConceptGraph()): List<Concept> {
            val found = ayahs.flatMapTo(HashSet()) { AyahAnalyzer.analyze(it).conceptIds }
            return graph.inLearningOrder(graph.withPrerequisites(found))
        }
    }
}

/** How much of a layer the learner knows (decision 9: per layer, and words as a share). */
data class LayerProgress(val known: Int, val total: Int) {
    val fraction: Float get() = if (total == 0) 1f else known.toFloat() / total
    val percent: Int get() = if (total == 0) 100 else known * 100 / total

    fun reaches(percent: Int): Boolean = known * 100 >= total * percent
}

/**
 * Readiness for surahs and stages. "Known" is every item the learner said they know or
 * passed a check on (decision 7: both count).
 */
object Readiness {
    /** Null for Words when the learning pack isn't downloaded. */
    fun of(needs: Collection<SurahNeeds>, layer: Layer, known: Set<String>): LayerProgress? {
        if (layer == Layer.WORDS) {
            if (needs.any { it.words == null }) return null
            val words = needs.flatMap { it.words.orEmpty() }
            return LayerProgress(words.count { word -> word.all { it in known } }, words.size)
        }
        // A concept needed by several surahs is one thing to learn, not several.
        val concepts = needs.flatMapTo(LinkedHashSet()) { surah -> surah.concepts.filter { it.track == layer.track } }
        return LayerProgress(concepts.count { it.id in known }, concepts.size)
    }

    /** Null when a surah of the goal isn't loaded, or its words need the pack: unknown, not 0 of 0. */
    fun of(goal: StageGoal, needsBySurah: Map<Int, SurahNeeds>, known: Set<String>): LayerProgress? = when (goal) {
        is StageGoal.Concepts -> LayerProgress(goal.conceptIds.count { it in known }, goal.conceptIds.size)
        is StageGoal.Surahs -> {
            val needs = goal.surahs.map { needsBySurah[it] ?: return null }
            of(needs, goal.layer, known)
        }
    }

    fun isReached(goal: StageGoal, needsBySurah: Map<Int, SurahNeeds>, known: Set<String>): Boolean {
        val progress = of(goal, needsBySurah, known) ?: return false
        return progress.reaches(if (goal is StageGoal.Surahs) goal.percent else 100)
    }

    /**
     * The dictionary words of [needs] the learner doesn't know yet, most frequent in the
     * surah first: learning them in this order raises the words % fastest.
     */
    fun unknownWords(needs: SurahNeeds, known: Set<String>): List<String> =
        needs.words.orEmpty()
            .flatten()
            .filter { it !in known }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
}
