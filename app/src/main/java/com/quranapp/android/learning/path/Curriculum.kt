package com.quranapp.android.learning.path

import androidx.annotation.StringRes
import com.quranapp.android.R
import com.quranapp.android.learning.concepts.ConceptIds
import com.quranapp.android.learning.letters.Letters

/** What finishing a stage means. */
sealed interface StageGoal {
    /** Know these concepts: stage 0's basics, until the letters arrive (milestone 7). */
    data class Concepts(val conceptIds: List<String>) : StageGoal

    /** Know at least [percent]% of what [layer] needs in [surahs]. */
    data class Surahs(val layer: Layer, val surahs: List<Int>, val percent: Int = 100) : StageGoal
}

/**
 * One stage of the path (decision 2). A stage is a suggestion, not a gate: every unit stays
 * open, and placement can start a learner further on.
 */
data class Stage(
    val number: Int,
    @StringRes val titleRes: Int,
    @StringRes val goalRes: Int,
    /** The surah units this stage adds to the path, in teaching order. */
    val units: List<Int>,
    val goals: List<StageGoal>,
)

/**
 * The seven stages from the letters to understanding any ayah, as in the approved design.
 * Each stage mixes the layers around a few surahs, so a beginner reads, recites and then
 * understands the same ayahs. Stages 3, 4 and 6 add no new surahs: they return to earlier
 * ones for their meaning, or aim at the whole Quran.
 */
object Curriculum {
    /** The last ten surahs, shortest and most familiar first. */
    val LAST_TEN = listOf(112, 113, 114, 108, 110, 111, 106, 109, 107, 105)

    val STAGE_ONE_SURAHS = listOf(1) + LAST_TEN

    /** The rest of Juz ʿAmma, from its end: short surahs first, as it is usually learned. */
    val JUZ_AMMA_REST = (104 downTo 78).toList()

    val JUZ_AMMA = (78..114).toList()

    /** Juz Tabārak, from Al-Mulk. */
    val JUZ_TABARAK = (67..77).toList()

    val WHOLE_QURAN = (1..114).toList()

    /**
     * Stage 0's goal: the 29 letters, then the vowel marks and the heavy letters. Knowing the
     * letters also completes the "Arabic letters" concept, which other concepts build on.
     */
    val BASICS = Letters.all.map { it.id } + listOf(
        ConceptIds.SHORT_VOWELS,
        ConceptIds.SUKUN,
        ConceptIds.SHADDA,
        ConceptIds.TANWEEN,
        ConceptIds.LONG_VOWELS,
        ConceptIds.HEAVY_LETTERS,
    )

    val stages: List<Stage> = listOf(
        Stage(
            number = 0,
            titleRes = R.string.learning_stage_0,
            goalRes = R.string.learning_stage_0_goal,
            units = emptyList(),
            goals = listOf(StageGoal.Concepts(BASICS)),
        ),
        Stage(
            number = 1,
            titleRes = R.string.learning_stage_1,
            goalRes = R.string.learning_stage_1_goal,
            units = STAGE_ONE_SURAHS,
            goals = listOf(
                StageGoal.Surahs(Layer.READ, STAGE_ONE_SURAHS),
                StageGoal.Surahs(Layer.RECITE, STAGE_ONE_SURAHS),
                StageGoal.Surahs(Layer.WORDS, listOf(1)),
            ),
        ),
        Stage(
            number = 2,
            titleRes = R.string.learning_stage_2,
            goalRes = R.string.learning_stage_2_goal,
            units = JUZ_AMMA_REST,
            goals = listOf(StageGoal.Surahs(Layer.RECITE, JUZ_AMMA)),
        ),
        Stage(
            number = 3,
            titleRes = R.string.learning_stage_3,
            goalRes = R.string.learning_stage_3_goal,
            units = emptyList(),
            goals = listOf(StageGoal.Surahs(Layer.WORDS, STAGE_ONE_SURAHS)),
        ),
        Stage(
            number = 4,
            titleRes = R.string.learning_stage_4,
            goalRes = R.string.learning_stage_4_goal,
            units = emptyList(),
            goals = listOf(StageGoal.Surahs(Layer.WORDS, JUZ_AMMA, percent = 95)),
        ),
        Stage(
            number = 5,
            titleRes = R.string.learning_stage_5,
            goalRes = R.string.learning_stage_5_goal,
            units = JUZ_TABARAK,
            goals = listOf(
                StageGoal.Surahs(Layer.RECITE, JUZ_TABARAK),
                StageGoal.Surahs(Layer.WORDS, JUZ_TABARAK, percent = 95),
            ),
        ),
        Stage(
            number = 6,
            titleRes = R.string.learning_stage_6,
            goalRes = R.string.learning_stage_6_goal,
            units = emptyList(),
            goals = listOf(StageGoal.Surahs(Layer.WORDS, WHOLE_QURAN, percent = 98)),
        ),
    )

    /** Every surah unit on the path, in order. */
    val units: List<Int> = stages.flatMap { it.units }

    /** The stage that first puts [surahNo] on the path, or null if the path doesn't list it. */
    fun stageOfUnit(surahNo: Int): Stage? = stages.firstOrNull { surahNo in it.units }
}
