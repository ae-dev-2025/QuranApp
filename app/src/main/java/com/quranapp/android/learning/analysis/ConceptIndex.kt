package com.quranapp.android.learning.analysis

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Where each reading and recitation concept occurs in the Quran, worked out ahead of time.
 *
 * The detectors read the Quran text that ships with the app, so their findings never change
 * on a phone. Analysing every ayah takes seconds, though: finding the three examples of the
 * saktas took 8 s on the emulator, and working out Al-Baqarah's rules 1.4 s. So the findings are
 * computed once, by `ShippedConceptIndexTest` from the same text and detectors, and shipped as
 * [ASSET]. That test fails whenever a detector changes and the file hasn't been made again.
 *
 * Ayahs are ids, as elsewhere in the app: surah × 1000 + ayah (2:255 is 2255).
 */
class ConceptIndex(private val ayahsByConcept: Map<String, IntArray>) {
    /** The ayahs where [conceptId] occurs, in the Quran's order; empty when it doesn't. */
    fun ayahsOf(conceptId: String): IntArray = ayahsByConcept[conceptId] ?: IntArray(0)

    /** The concepts found in a surah's ayahs: what [AyahAnalyzer] finds in them, all together. */
    fun conceptsOf(surahNo: Int): Set<String> = ayahsByConcept.filterValues { ayahs ->
        // Ayahs are sorted: find where the surah's would be, and see if one is there.
        val at = ayahs.binarySearch(surahNo * 1000).let { if (it < 0) -it - 1 else it }
        at < ayahs.size && ayahs[at] / 1000 == surahNo
    }.keys

    /**
     * The index as text, one concept a line with its ayahs as runs: `reading.sakta 18001,36052`
     * or `reading.short_vowels 1001-1007,2001-2286`. Concepts in id order, so the file only
     * changes where the findings do.
     */
    fun write(out: Appendable) {
        out.append(HEADER)
        for (conceptId in ayahsByConcept.keys.sorted()) {
            out.append(conceptId).append(' ')
            val ayahs = ayahsByConcept.getValue(conceptId)
            var i = 0
            while (i < ayahs.size) {
                var end = i
                while (end + 1 < ayahs.size && ayahs[end + 1] == ayahs[end] + 1) end++
                if (i > 0) out.append(',')
                out.append(ayahs[i].toString())
                if (end > i) out.append('-').append(ayahs[end].toString())
                i = end + 1
            }
            out.append('\n')
        }
    }

    override fun equals(other: Any?): Boolean = other is ConceptIndex &&
        other.ayahsByConcept.keys == ayahsByConcept.keys &&
        ayahsByConcept.all { (id, ayahs) -> ayahs.contentEquals(other.ayahsByConcept.getValue(id)) }

    override fun hashCode(): Int = ayahsByConcept.keys.hashCode()

    companion object {
        /** The shipped index, in the app's assets. */
        const val ASSET = "learning/concept_index.txt"

        private const val HEADER =
            "# Where each reading and recitation concept occurs: ayah ids (surah * 1000 + ayah).\n" +
                "# Made by ShippedConceptIndexTest from the app's Quran text; don't edit by hand.\n"

        /** Runs the detectors over [ayahs] (each ayah id with its Uthmani words). */
        fun of(ayahs: Map<Int, List<String>>): ConceptIndex {
            val found = HashMap<String, MutableList<Int>>()
            for (ayahId in ayahs.keys.sorted()) {
                for (conceptId in AyahAnalyzer.analyze(ayahs.getValue(ayahId)).conceptIds) {
                    found.getOrPut(conceptId) { mutableListOf() } += ayahId
                }
            }
            return ConceptIndex(found.mapValues { (_, ids) -> ids.toIntArray() })
        }

        /**
         * Reads what [write] wrote. It's read on the way to the Learn screen, so digit by digit
         * straight into arrays: splitting strings and boxing each of ~200,000 ayahs took 0.85 s.
         */
        fun parse(lines: Sequence<String>): ConceptIndex {
            val index = HashMap<String, IntArray>()
            var ayahs = IntArray(1024)
            for (line in lines) {
                if (line.isBlank() || line.startsWith("#")) continue
                val space = line.indexOf(' ')
                var count = 0
                var i = space + 1
                while (i < line.length) {
                    var first = 0
                    while (i < line.length && line[i].isDigit()) first = first * 10 + (line[i++] - '0')
                    var last = first
                    if (i < line.length && line[i] == '-') {
                        i++
                        last = 0
                        while (i < line.length && line[i].isDigit()) last = last * 10 + (line[i++] - '0')
                    }
                    i++ // the comma
                    if (count + (last - first + 1) > ayahs.size) ayahs = ayahs.copyOf(maxOf(ayahs.size * 2, count + last - first + 1))
                    for (ayahId in first..last) ayahs[count++] = ayahId
                }
                index[line.substring(0, space)] = ayahs.copyOf(count)
            }
            return ConceptIndex(index)
        }

        private val lock = Mutex()

        @Volatile
        private var shipped: ConceptIndex? = null

        /** The shipped index, read once per app run; null if it's missing, and then callers analyse the text themselves. */
        suspend fun get(context: Context): ConceptIndex? = shipped ?: lock.withLock {
            shipped ?: withContext(Dispatchers.IO) {
                runCatching { context.assets.open(ASSET).bufferedReader().useLines(::parse) }.getOrNull()
            }?.also { shipped = it }
        }
    }
}
