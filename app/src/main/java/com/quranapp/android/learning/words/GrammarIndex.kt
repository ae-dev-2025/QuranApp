package com.quranapp.android.learning.words

import android.content.Context
import com.quranapp.android.learning.analysis.ConceptIndex
import com.quranapp.android.learning.pack.LearningPackManager
import com.quranapp.android.learning.pack.LearningPackRelease
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Where each grammar concept occurs in the Quran: a [ConceptIndex], like the reading one, but
 * made on the phone. Grammar is found in the downloaded learning pack's analysis of each word,
 * so it can't ship with the app. Finding it for Al-Baqarah alone takes 2 s on the emulator.
 *
 * So the whole Quran's grammar is worked out once, in the background (about 9 s on the
 * emulator), and kept next to the pack (deleted with it). It's marked with what it was made
 * from: the pack's version and when the app was installed or updated. A detector only changes
 * with an app update, so an update makes the index again, and a stale one is never used.
 * Until it's ready, [get] returns null and callers work grammar out from the pack as before.
 */
object GrammarIndex {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val lock = Mutex()

    @Volatile
    private var ready: Pair<String, ConceptIndex>? = null
    private var making: Job? = null

    /** The index for the installed pack; null without the pack, or while it's being made (and then it starts making it). */
    suspend fun get(context: Context): ConceptIndex? {
        val app = context.applicationContext
        if (!LearningPackManager.file(app).exists()) return null
        val madeFrom = madeFrom(app)
        ready?.let { (from, index) -> if (from == madeFrom) return index }
        return lock.withLock {
            ready?.takeIf { it.first == madeFrom }?.second
                ?: withContext(Dispatchers.IO) { read(app, madeFrom) }?.also { ready = madeFrom to it }
                ?: null.also { if (making?.isActive != true) making = scope.launch { make(app, madeFrom) } }
        }
    }

    private suspend fun make(app: Context, madeFrom: String) {
        val index = runCatching {
            val words = WordRepository.open(app) ?: return
            val found = HashMap<Int, Set<String>>()
            for (surahNo in 1..114) {
                for ((ayahId, grammar) in words.grammarByAyahOfSurah(surahNo)) found[ayahId] = grammar.keys
            }
            ConceptIndex.from(found)
        }.getOrNull() ?: return // the pack was deleted meanwhile: nothing to keep
        withContext(Dispatchers.IO) {
            runCatching {
                // Written whole and then renamed, so a half-written file is never read.
                val part = File(file(app).path + ".part")
                part.bufferedWriter().use { out -> index.write(out, header = "$MADE_FROM$madeFrom\n") }
                part.renameTo(file(app))
            }
        }
        lock.withLock { ready = madeFrom to index }
    }

    private fun read(app: Context, madeFrom: String): ConceptIndex? = runCatching {
        file(app).bufferedReader().useLines { lines ->
            val all = lines.iterator()
            if (!all.hasNext() || all.next() != "$MADE_FROM$madeFrom") null else ConceptIndex.parse(all.asSequence())
        }
    }.getOrNull()

    /** Next to the pack, and named after it, so the pack's own clean-up keeps it and deletes it with the pack. */
    private fun file(app: Context) = LearningPackManager.file(app).let { File(it.parentFile, it.name + ".grammar_index.txt") }

    private fun madeFrom(app: Context): String {
        val installed = runCatching { app.packageManager.getPackageInfo(app.packageName, 0).lastUpdateTime }.getOrDefault(0L)
        return "pack v${LearningPackRelease.VERSION}, app installed $installed"
    }

    private const val MADE_FROM = "# made from: "
}
