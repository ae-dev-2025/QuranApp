package com.quranapp.android.learning.concepts

import androidx.annotation.StringRes

/**
 * The areas of knowledge a learner progresses through.
 * Grammar and vocabulary tracks will be added in later milestones.
 */
enum class Track {
    /** Reading the Arabic script: letters, vowel marks and the special marks of the muṣḥaf. */
    READING,

    /** Rules of recitation. */
    TAJWEED,
}

/**
 * One thing a learner can know, e.g. "sukun" or "ikhfa".
 *
 * @property id Stable identifier, one of [ConceptIds]. It is stored in the user's progress,
 * so it must never change once released.
 * @property titleRes Short name shown in lists.
 * @property summaryRes One or two sentences explaining the concept.
 * @property prerequisites IDs of the concepts that should be learned before this one.
 */
data class Concept(
    val id: String,
    val track: Track,
    @StringRes val titleRes: Int,
    @StringRes val summaryRes: Int,
    val prerequisites: List<String> = emptyList(),
)
