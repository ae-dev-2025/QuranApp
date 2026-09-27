package com.quranapp.android.learning.path

/** A unit's state for one layer: a dot on the path (decision 9). */
enum class Dot { EMPTY, PARTIAL, FULL, UNKNOWN }

/** Where the learner is on the path. Pure logic: the surahs come in through [load]. */
object PathProgress {
    /** A surah's words count as learned at 95%: reading it with the odd look-up. */
    const val WORDS_DONE_PERCENT = 95

    fun dot(progress: LayerProgress?, layer: Layer): Dot = when {
        progress == null -> Dot.UNKNOWN
        progress.reaches(if (layer == Layer.WORDS) WORDS_DONE_PERCENT else 100) -> Dot.FULL
        progress.known > 0 -> Dot.PARTIAL
        else -> Dot.EMPTY
    }

    /** The surahs a stage's goals are counted over, in path order. */
    fun goalSurahs(stage: Stage): List<Int> {
        val surahs = stage.goals.filterIsInstance<StageGoal.Surahs>().flatMap { it.surahs }.distinct()
        val order = Curriculum.units.withIndex().associate { (index, surah) -> surah to index }
        return surahs.sortedWith(compareBy({ order[it] ?: Int.MAX_VALUE }, { it }))
    }

    /**
     * The first stage from [startAt] whose goals aren't all reached, or the last stage when
     * every goal is. Stages are checked in order and each loads only its own surahs, so a
     * beginner never waits for the whole Quran to be analysed.
     */
    suspend fun currentStage(
        known: Set<String>,
        load: suspend (Collection<Int>) -> Map<Int, SurahNeeds>,
        startAt: Int = 0,
    ): Stage {
        val stages = Curriculum.stages.filter { it.number >= startAt }
        for (stage in stages) {
            val needs = load(goalSurahs(stage))
            if (!stage.goals.all { Readiness.isReached(it, needs, known) }) return stage
        }
        return stages.last()
    }

    /**
     * The surah to continue with in [stage]: the first one, in path order, where one of the
     * stage's goals isn't reached yet for that surah. Null for stage 0 (it has no surahs).
     */
    fun nextUnit(stage: Stage, needsBySurah: Map<Int, SurahNeeds>, known: Set<String>): Int? =
        goalSurahs(stage).firstOrNull { surah ->
            stage.goals.filterIsInstance<StageGoal.Surahs>()
                .filter { surah in it.surahs }
                .any { !Readiness.isReached(it.copy(surahs = listOf(surah)), needsBySurah, known) }
        }

    /** Stage 0's next concept: the first basic the learner doesn't know yet. */
    fun nextConcept(stage: Stage, known: Set<String>): String? =
        stage.goals.filterIsInstance<StageGoal.Concepts>().flatMap { it.conceptIds }.firstOrNull { it !in known }
}
