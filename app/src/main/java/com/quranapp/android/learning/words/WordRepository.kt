package com.quranapp.android.learning.words

import android.content.Context
import com.quranapp.android.learning.pack.LearningPackDao
import com.quranapp.android.learning.pack.LearningPackManager
import com.quranapp.android.learning.pack.LemmaEntity
import com.quranapp.android.learning.pack.RootEntity
import com.quranapp.android.learning.pack.SegmentEntity
import com.quranapp.android.learning.pack.SyntaxEntity
import com.quranapp.android.learning.pack.WordGlossEntity
import com.quranapp.android.learning.pack.WordLocation

/** Reads words from the learning pack and puts each one's pieces together. */
class WordRepository(private val dao: LearningPackDao) {
    /** The ayah's words in order, one [AyahWord] per word of the app's text. */
    suspend fun wordsOfAyah(ayahId: Int): List<AyahWord> {
        val segments = dao.segmentsOfAyah(ayahId)
        val lemmaIds = segments.mapNotNull { it.lemmaId }.distinct()
        val lemmas = if (lemmaIds.isEmpty()) emptyList() else dao.lemmas(lemmaIds)
        val rootIds = lemmas.mapNotNull { it.rootId }.distinct()
        val roots = if (rootIds.isEmpty()) emptyList() else dao.roots(rootIds)
        return assemble(ayahId, segments, lemmas, roots, dao.glossesOfAyah(ayahId), dao.syntaxOfAyah(ayahId))
    }

    /** Every place a dictionary word occurs, in Quran order. */
    suspend fun occurrences(lemmaId: Int): List<WordLocation> = dao.occurrencesOfLemma(lemmaId, Int.MAX_VALUE)

    /** A root by its stable key (`Ebd`), with its dictionary words; null if the pack has no such root. */
    suspend fun root(rootKey: String): RootWithLemmas? {
        val root = dao.rootByKey(rootKey) ?: return null
        return RootWithLemmas(root, dao.lemmasOfRoot(root.rootId))
    }

    companion object {
        /** The repository, or null when the learning pack isn't downloaded. */
        suspend fun open(context: Context): WordRepository? =
            LearningPackManager.database(context)?.let { WordRepository(it.dao()) }

        /** Groups the pack's rows into words. Pure, so it's unit-tested without a database. */
        fun assemble(
            ayahId: Int,
            segments: List<SegmentEntity>,
            lemmas: List<LemmaEntity>,
            roots: List<RootEntity>,
            glosses: List<WordGlossEntity>,
            syntax: List<SyntaxEntity>,
        ): List<AyahWord> {
            val lemmaById = lemmas.associateBy { it.lemmaId }
            val rootById = roots.associateBy { it.rootId }
            val glossByWord = glosses.associate { it.wordIndex to it.gloss }
            val syntaxByWord = syntax.groupBy { it.wordIndex }

            return segments.groupBy { it.wordIndex }.toSortedMap().map { (wordIndex, wordSegments) ->
                val ordered = wordSegments.sortedBy { it.segmentIndex }
                AyahWord(
                    ayahId = ayahId,
                    wordIndex = wordIndex,
                    segments = ordered,
                    lemmas = ordered
                        .mapNotNull { segment -> segment.lemmaId?.let(lemmaById::get) }
                        .distinctBy { it.lemmaId }
                        .map { WordLemma(it, it.rootId?.let(rootById::get)) },
                    gloss = glossByWord[wordIndex],
                    syntax = syntaxByWord[wordIndex].orEmpty().sortedBy { it.segmentIndex },
                )
            }
        }
    }
}
