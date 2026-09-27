package com.quranapp.android.learning.progress

import com.quranapp.android.learning.concepts.ConceptCatalog
import com.quranapp.android.learning.words.WordItems
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * The learning part of the app's Export/Import file ("Keep it"): what the learner knows and
 * when each item should be reviewed. Only item ids, grades and times: nothing about the person.
 *
 * The short field names keep a file with thousands of reviews small.
 */
@Serializable
data class LearningBackup(
    val format: Int = FORMAT,
    val known: List<KnownEntry> = emptyList(),
    val cards: List<CardEntry> = emptyList(),
    val log: List<LogEntry> = emptyList(),
) {
    companion object {
        /** Raise when the layout changes; older apps then refuse files they can't read. */
        const val FORMAT = 1
    }
}

/** An item the learner said they know, or passed a check on. */
@Serializable
data class KnownEntry(val id: String, val at: Long)

/** A review card: see [ReviewCardEntity]. `due` and `last` are milliseconds since 1970. */
@Serializable
data class CardEntry(
    val id: String,
    val state: String,
    val stability: Double,
    val difficulty: Double,
    val due: Long,
    val last: Long,
    val reps: Int,
    val lapses: Int,
)

/** One answered review: see [ReviewLogEntity]. */
@Serializable
data class LogEntry(val id: String, val rating: String, val at: Long, val before: String? = null)

/** A backup that passed every check, ready to merge into the database. */
data class CheckedBackup(
    val known: List<ConceptProgressEntity>,
    val cards: List<ReviewCardEntity>,
    val log: List<ReviewLogEntity>,
    /** Entries that were left out because they were broken or unknown to this version. */
    val skipped: Int,
)

/** Why a whole learning section was refused. */
class BackupRefusedException(val reason: Reason) : IllegalArgumentException(reason.name) {
    enum class Reason { NEWER_FORMAT, TOO_LARGE }
}

/** Reads, writes and checks [LearningBackup]s. Pure Kotlin, so it is tested on the JVM. */
object LearningBackups {
    /** No real learner comes close to these; a file above them is refused as a whole. */
    const val MAX_KNOWN = 50_000
    const val MAX_CARDS = 50_000
    const val MAX_LOG = 500_000

    /** 2020-01-01: nothing in a real backup is older than this app's learning mode. */
    private const val EARLIEST = 1_577_836_800_000L
    private const val DAY_MS = 86_400_000L

    /**
     * Unknown fields are ignored so a newer app's extra fields don't break an older one.
     * Defaults are written, so every file states its `format`; empty fields are left out.
     */
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    fun toJson(backup: LearningBackup): String = json.encodeToString(LearningBackup.serializer(), backup)

    /** Throws if the section isn't shaped like a backup at all. */
    fun fromJson(element: JsonElement): LearningBackup = json.decodeFromJsonElement(LearningBackup.serializer(), element)

    fun of(known: List<ConceptProgressEntity>, cards: List<ReviewCardEntity>, log: List<ReviewLogEntity>) = LearningBackup(
        known = known.filter { it.status == ConceptStatus.KNOWN }.map { KnownEntry(it.conceptId, it.updatedAt) },
        cards = cards.map { CardEntry(it.itemId, it.state.name, it.stability, it.difficulty, it.dueAt, it.lastReviewAt, it.reps, it.lapses) },
        log = log.map { LogEntry(it.itemId, it.rating.name, it.reviewedAt, it.stateBefore?.name) },
    )

    /**
     * Checks every entry of [backup], made on another phone or edited by hand, before any of
     * it reaches the database. Broken entries are skipped and counted; a file from a newer
     * format or with an unreasonable number of entries is refused with [BackupRefusedException].
     */
    fun check(backup: LearningBackup, now: Long, isItem: (String) -> Boolean = LearningItems::isValid): CheckedBackup {
        if (backup.format > LearningBackup.FORMAT) throw BackupRefusedException(BackupRefusedException.Reason.NEWER_FORMAT)
        if (backup.known.size > MAX_KNOWN || backup.cards.size > MAX_CARDS || backup.log.size > MAX_LOG) {
            throw BackupRefusedException(BackupRefusedException.Reason.TOO_LARGE)
        }
        val latest = now + DAY_MS // allow for another phone's clock being a little ahead
        fun isTime(t: Long) = t in EARLIEST..latest

        val known = backup.known.mapNotNull { entry ->
            if (isItem(entry.id) && isTime(entry.at)) ConceptProgressEntity(entry.id, ConceptStatus.KNOWN, entry.at) else null
        }
        val cards = backup.cards.mapNotNull { entry ->
            val state = enumOrNull<ReviewState>(entry.state) ?: return@mapNotNull null
            val valid = isItem(entry.id) &&
                entry.stability.isFinite() && entry.stability > 0 && entry.stability <= 100_000 &&
                entry.difficulty in 1.0..10.0 &&
                isTime(entry.last) && entry.due in entry.last..(latest + 36_600 * DAY_MS) &&
                entry.reps in 0..1_000_000 && entry.lapses in 0..entry.reps
            if (!valid) return@mapNotNull null
            ReviewCardEntity(entry.id, state, entry.stability, entry.difficulty, entry.due, entry.last, entry.reps, entry.lapses)
        }
        val log = backup.log.mapNotNull { entry ->
            val rating = enumOrNull<ReviewRating>(entry.rating) ?: return@mapNotNull null
            val before = entry.before?.let { enumOrNull<ReviewState>(it) ?: return@mapNotNull null }
            if (!isItem(entry.id) || !isTime(entry.at)) return@mapNotNull null
            ReviewLogEntity(itemId = entry.id, rating = rating, reviewedAt = entry.at, stateBefore = before)
        }

        val skipped = (backup.known.size - known.size) + (backup.cards.size - cards.size) + (backup.log.size - log.size)
        return CheckedBackup(
            known = known.distinctBy { it.conceptId },
            // One card per item: the most recently reviewed one.
            cards = cards.groupBy { it.itemId }.values.map { same -> same.maxBy { it.lastReviewAt } },
            log = log.distinctBy { Triple(it.itemId, it.reviewedAt, it.rating) },
            skipped = skipped,
        )
    }

    /** When both phones have a card for an item, the more recent review wins; a tie keeps ours. */
    fun merge(ours: ReviewCardEntity?, theirs: ReviewCardEntity): ReviewCardEntity =
        if (ours == null || theirs.lastReviewAt > ours.lastReviewAt) theirs else ours

    private inline fun <reified T : Enum<T>> enumOrNull(name: String): T? = enumValues<T>().firstOrNull { it.name == name }
}

/** Which item ids this version of the app understands. */
object LearningItems {
    /** Lemma keys are Buckwalter: printable ASCII without spaces, 15 characters at most today. */
    private val LEMMA_KEY = Regex("[!-~]{1,40}")

    fun isValid(itemId: String): Boolean {
        if (ConceptCatalog[itemId] != null) return true
        val lemmaKey = WordItems.lemmaKeyOf(itemId) ?: return false
        return LEMMA_KEY.matches(lemmaKey)
    }
}
