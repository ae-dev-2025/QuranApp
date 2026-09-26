package com.quranapp.android.learning.progress

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The one place the UI goes to read or change learning progress. Screens don't talk to
 * the DAO directly, so the storage can change later without touching them.
 */
class LearningProgressRepository(
    private val dao: ConceptProgressDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    /** IDs of the concepts the learner knows. Updates automatically when progress changes. */
    val knownConceptIds: Flow<Set<String>> = dao.observeKnownIds().map { it.toSet() }

    /** Marks a concept as known, or back to new when [known] is false. */
    suspend fun setKnown(conceptId: String, known: Boolean) {
        if (known) {
            dao.upsert(ConceptProgressEntity(conceptId, ConceptStatus.KNOWN, clock()))
        } else {
            dao.delete(conceptId)
        }
    }
}
