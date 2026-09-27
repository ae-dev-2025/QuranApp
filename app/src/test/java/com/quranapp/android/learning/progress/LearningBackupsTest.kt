package com.quranapp.android.learning.progress

import com.quranapp.android.learning.concepts.ConceptIds
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningBackupsTest {
    private val now = 1_790_000_000_000L // September 2026
    private val day = 86_400_000L

    private fun card(itemId: String, last: Long, due: Long = last + 3 * day, reps: Int = 2, lapses: Int = 0) =
        ReviewCardEntity(itemId, ReviewState.REVIEW, 3.2, 5.1, due, last, reps, lapses)

    private fun checked(backup: LearningBackup) = LearningBackups.check(backup, now)

    @Test
    fun exportThenImport_givesTheSameProgress() {
        val known = listOf(ConceptProgressEntity(ConceptIds.SUKUN, ConceptStatus.KNOWN, now - day))
        val cards = listOf(card("word.Eabod", now - day), card(ConceptIds.SUKUN, now - 2 * day))
        val log = listOf(
            ReviewLogEntity(id = 1, itemId = "word.Eabod", rating = ReviewRating.GOOD, reviewedAt = now - day, stateBefore = null),
            ReviewLogEntity(id = 2, itemId = ConceptIds.SUKUN, rating = ReviewRating.AGAIN, reviewedAt = now - 2 * day, stateBefore = ReviewState.LEARNING),
        )

        val text = LearningBackups.toJson(LearningBackups.of(known, cards, log))
        val back = checked(LearningBackups.fromJson(Json.parseToJsonElement(text)))

        assertEquals(known, back.known)
        assertEquals(cards.toSet(), back.cards.toSet())
        // The database gives log rows new ids; everything else survives.
        assertEquals(log.map { it.copy(id = 0) }, back.log)
        assertEquals(0, back.skipped)
    }

    @Test
    fun everyFileStatesItsFormat() {
        val text = LearningBackups.toJson(LearningBackup(log = listOf(LogEntry("word.Eabod", "GOOD", now))))
        assertTrue(text, text.contains("\"format\":1"))
        assertFalse("a first review has no state before it", text.contains("before"))
    }

    @Test
    fun settingsTheAppDoesNotKnowAreDropped() {
        val settings = LearningSettingsEntry(start = 9, goal = 115, newWordsPerDay = 7).checked()
        assertEquals(LearningSettingsEntry(start = -1, goal = 0, newWordsPerDay = null), settings)
        assertEquals(LearningSettingsEntry(1, 18, 5), LearningSettingsEntry(1, 18, 5).checked())
        // A file from before settings were exported has none.
        assertEquals(null, checked(LearningBackup()).settings)
    }

    @Test
    fun onlyKnownItemsAreExported() {
        val learning = ConceptProgressEntity(ConceptIds.SHADDA, ConceptStatus.LEARNING, now)
        assertTrue(LearningBackups.of(listOf(learning), emptyList(), emptyList()).known.isEmpty())
    }

    @Test
    fun brokenEntriesAreSkippedAndCounted() {
        val good = CardEntry("word.Eabod", "REVIEW", 3.0, 5.0, now + day, now - day, 2, 0)
        val backup = LearningBackup(
            known = listOf(
                KnownEntry(ConceptIds.SUKUN, now - day),
                KnownEntry("reading.not_a_concept", now - day), // unknown to this version
                KnownEntry("word.", now - day), // no lemma
                KnownEntry("word.a b", now - day), // a space can't be in a lemma key
                KnownEntry(ConceptIds.SHADDA, now + 30 * day), // in the future
            ),
            cards = listOf(
                good,
                good.copy(state = "FORGOTTEN"),
                good.copy(stability = Double.POSITIVE_INFINITY),
                good.copy(stability = -1.0),
                good.copy(difficulty = 11.0),
                good.copy(due = good.last - 1), // due before it was reviewed
                good.copy(lapses = 5, reps = 2),
                good.copy(last = 0), // 1970
            ),
            log = listOf(
                LogEntry("word.Eabod", "GOOD", now - day),
                LogEntry("word.Eabod", "PERFECT", now - day),
                LogEntry("word.Eabod", "GOOD", now - day, before = "SLEEPING"),
            ),
        )
        val result = checked(backup)
        assertEquals(listOf(ConceptIds.SUKUN), result.known.map { it.conceptId })
        assertEquals(1, result.cards.size)
        assertEquals(1, result.log.size)
        assertEquals(4 + 7 + 2, result.skipped)
    }

    @Test
    fun aFileFromANewerFormatIsRefused() {
        val error = assertThrows(BackupRefusedException::class.java) {
            checked(LearningBackup(format = LearningBackup.FORMAT + 1))
        }
        assertEquals(BackupRefusedException.Reason.NEWER_FORMAT, error.reason)
    }

    @Test
    fun anUnreasonablyLargeFileIsRefused() {
        val many = List(LearningBackups.MAX_KNOWN + 1) { KnownEntry(ConceptIds.SUKUN, now) }
        val error = assertThrows(BackupRefusedException::class.java) { checked(LearningBackup(known = many)) }
        assertEquals(BackupRefusedException.Reason.TOO_LARGE, error.reason)
    }

    @Test
    fun somethingThatIsNotABackupDoesNotParse() {
        assertThrows(Exception::class.java) {
            LearningBackups.fromJson(Json.parseToJsonElement("""{"known": "everything"}"""))
        }
    }

    @Test
    fun unknownFieldsFromANewerAppAreIgnored() {
        val text = """{"format":1,"known":[{"id":"${ConceptIds.SUKUN}","at":$now,"note":"new"}],"streak":12}"""
        assertEquals(1, checked(LearningBackups.fromJson(Json.parseToJsonElement(text))).known.size)
    }

    @Test
    fun duplicatesInTheFileKeepTheMostRecentCard() {
        val older = CardEntry("word.Eabod", "REVIEW", 1.0, 5.0, now, now - 5 * day, 1, 0)
        val newer = older.copy(stability = 4.0, due = now + 4 * day, last = now - day, reps = 2)
        val result = checked(LearningBackup(cards = listOf(newer, older)))
        assertEquals(1, result.cards.size)
        assertEquals(4.0, result.cards.single().stability, 0.0)
    }

    @Test
    fun merge_keepsTheMoreRecentReview() {
        val ours = card("word.Eabod", last = now - day)
        val theirsOlder = card("word.Eabod", last = now - 3 * day)
        val theirsNewer = card("word.Eabod", last = now - 1)
        assertSame(ours, LearningBackups.merge(ours, theirsOlder))
        assertSame(theirsNewer, LearningBackups.merge(ours, theirsNewer))
        assertSame(theirsOlder, LearningBackups.merge(null, theirsOlder))
        assertSame("a tie keeps ours", ours, LearningBackups.merge(ours, ours.copy()))
    }

    @Test
    fun learningItems_knowsConceptsAndWords() {
        assertTrue(LearningItems.isValid(ConceptIds.IKHFA))
        assertTrue(LearningItems.isValid("word.S~a`bi_#iyn"))
        assertTrue(LearningItems.isValid("word.PRON:3MS"))
        assertFalse(LearningItems.isValid("letter.b")) // not in this version yet
        assertFalse(LearningItems.isValid("word." + "a".repeat(41)))
        assertFalse(LearningItems.isValid("word.عبد"))
    }
}
