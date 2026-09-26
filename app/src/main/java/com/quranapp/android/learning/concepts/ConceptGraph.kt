package com.quranapp.android.learning.concepts

/**
 * Answers questions about the prerequisite graph formed by a list of concepts.
 *
 * The list must be in teaching order (prerequisites first), like [ConceptCatalog.all].
 * Thanks to that, "the right order to learn a set of concepts" is just the list order
 * filtered down to that set. No graph sorting algorithm is needed.
 */
class ConceptGraph(private val concepts: List<Concept> = ConceptCatalog.all) {
    private val byId: Map<String, Concept> = concepts.associateBy { it.id }
    private val position: Map<String, Int> =
        concepts.withIndex().associate { (index, concept) -> concept.id to index }

    /**
     * Returns [ids] plus everything they depend on, directly or indirectly.
     * IDs that are not in the graph are ignored.
     */
    fun withPrerequisites(ids: Collection<String>): Set<String> {
        val result = mutableSetOf<String>()
        val toVisit = ids.toMutableList()

        while (toVisit.isNotEmpty()) {
            val id = toVisit.removeAt(toVisit.lastIndex)
            val concept = byId[id] ?: continue

            // add() returns false if we've already been here; skip to avoid repeated work.
            if (result.add(id)) {
                toVisit.addAll(concept.prerequisites)
            }
        }

        return result
    }

    /** The given concepts sorted so that prerequisites come first. Unknown IDs are dropped. */
    fun inLearningOrder(ids: Collection<String>): List<Concept> {
        return ids.toSet()
            .mapNotNull { byId[it] }
            .sortedBy { position.getValue(it.id) }
    }

    /**
     * Concepts the learner doesn't know yet but can start now, because all their
     * prerequisites are in [known]. Knowledge-space theory calls this the "outer fringe".
     */
    fun readyToLearn(known: Set<String>): List<Concept> {
        return concepts.filter { concept ->
            concept.id !in known && concept.prerequisites.all { it in known }
        }
    }

    /** The direct prerequisites of a concept, in learning order. */
    fun prerequisitesOf(conceptId: String): List<Concept> {
        return inLearningOrder(byId[conceptId]?.prerequisites.orEmpty())
    }

    /** Concepts that list [conceptId] as a direct prerequisite, i.e. what it unlocks. */
    fun dependentsOf(conceptId: String): List<Concept> {
        return concepts.filter { conceptId in it.prerequisites }
    }
}
