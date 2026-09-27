package com.quranapp.android.learning.path

import com.quranapp.android.learning.concepts.ConceptCatalog
import com.quranapp.android.learning.letters.Letters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumTest {
    @Test
    fun stagesRunFromZeroToSix() {
        assertEquals((0..6).toList(), Curriculum.stages.map { it.number })
    }

    @Test
    fun everyUnitIsOnThePathOnce() {
        val units = Curriculum.units
        assertEquals(units.size, units.toSet().size)
        // Al-Fātiḥah, all of Juz ʿAmma and Juz Tabārak.
        assertEquals((listOf(1) + (67..114)).toSet(), units.toSet())
    }

    @Test
    fun stageOneIsAlFatihahAndTheLastTen() {
        assertEquals(1, Curriculum.units.first())
        assertEquals((listOf(1) + (105..114)).toSet(), Curriculum.stages[1].units.toSet())
        assertEquals(1, Curriculum.stageOfUnit(112)?.number)
        assertEquals(2, Curriculum.stageOfUnit(78)?.number)
        assertEquals(5, Curriculum.stageOfUnit(67)?.number)
    }

    @Test
    fun goalsNameRealSurahsAndConcepts() {
        Curriculum.stages.flatMap { it.goals }.forEach { goal ->
            when (goal) {
                is StageGoal.Concepts -> goal.conceptIds.forEach { assertNotNull(it, ConceptCatalog[it] ?: Letters[it]) }
                is StageGoal.Surahs -> {
                    assertTrue(goal.surahs.isNotEmpty() && goal.surahs.all { it in 1..114 })
                    assertTrue(goal.percent in 1..100)
                }
            }
        }
    }

    @Test
    fun everyStageHasAGoal() {
        Curriculum.stages.forEach { assertTrue("stage ${it.number}", it.goals.isNotEmpty()) }
    }
}
