package com.quranapp.android.learning.path

import com.quranapp.android.learning.analysis.AyahAnalyzer
import com.quranapp.android.learning.concepts.Concept
import com.quranapp.android.learning.concepts.ConceptGraph
import com.quranapp.android.learning.concepts.ConceptStages
import com.quranapp.android.learning.concepts.Track

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
    /** The grammar concepts its words show, prerequisites included; null without the pack. */
    val grammar: List<Concept>? = null,
) {
    companion object {
        /** [ayahs] are the Uthmani words of each ayah, as the app stores them. */
        fun conceptsOf(ayahs: List<List<String>>, graph: ConceptGraph = ConceptGraph()): List<Concept> =
            conceptsFound(ayahs.flatMapTo(HashSet()) { AyahAnalyzer.analyze(it).conceptIds }, graph)

        /** Concepts [found] in a surah's text (by the detectors or [ConceptIndex]), with their prerequisites, in learning order. */
        fun conceptsFound(found: Set<String>, graph: ConceptGraph = ConceptGraph()): List<Concept> =
            graph.inLearningOrder(graph.withPrerequisites(found))

        /** Grammar concepts [found] in a surah, with the grammar they build on, in learning order. */
        fun grammarOf(found: Set<String>, graph: ConceptGraph = ConceptGraph()): List<Concept> =
            graph.inLearningOrder(graph.withPrerequisites(found)).filter { it.track == Track.GRAMMAR }
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
    /**
     * Null for Words when the learning pack isn't downloaded. [upToStage] leaves out concepts
     * a later stage teaches (see [ConceptStages]); by default everything counts.
     */
    fun of(needs: Collection<SurahNeeds>, layer: Layer, known: Set<String>, upToStage: Int = Int.MAX_VALUE): LayerProgress? {
        if (layer == Layer.WORDS) {
            if (needs.any { it.words == null }) return null
            val words = needs.flatMap { it.words.orEmpty() }
            return LayerProgress(words.count { word -> word.all { it in known } }, words.size)
        }
        if (layer == Layer.GRAMMAR && needs.any { it.grammar == null }) return null
        // A concept needed by several surahs is one thing to learn, not several.
        val concepts = needs.flatMapTo(LinkedHashSet()) { surah ->
            val ofLayer = if (layer == Layer.GRAMMAR) surah.grammar.orEmpty() else surah.concepts
            ofLayer.filter { it.track == layer.track && ConceptStages.of(it.id) <= upToStage }
        }
        return LayerProgress(concepts.count { it.id in known }, concepts.size)
    }

    /** Null when a surah of the goal isn't loaded, or its words need the pack: unknown, not 0 of 0. */
    fun of(goal: StageGoal, needsBySurah: Map<Int, SurahNeeds>, known: Set<String>, stage: Int = Int.MAX_VALUE): LayerProgress? = when (goal) {
        is StageGoal.Concepts -> LayerProgress(goal.conceptIds.count { it in known }, goal.conceptIds.size)
        is StageGoal.Surahs -> {
            val needs = goal.surahs.map { needsBySurah[it] ?: return null }
            of(needs, goal.layer, known, upToStage = stage)
        }
    }

    /** [stage] is the number of the stage the goal belongs to. */
    fun isReached(goal: StageGoal, needsBySurah: Map<Int, SurahNeeds>, known: Set<String>, stage: Int = Int.MAX_VALUE): Boolean {
        val progress = of(goal, needsBySurah, known, stage) ?: return false
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
