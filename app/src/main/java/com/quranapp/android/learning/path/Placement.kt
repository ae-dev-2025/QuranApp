package com.quranapp.android.learning.path

import androidx.annotation.StringRes
import com.quranapp.android.R
import com.quranapp.android.compose.utils.preferences.DataStoreManager
import com.quranapp.android.compose.utils.preferences.PrefKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow

/**
 * Where a new learner starts (decision 11): three plain choices instead of a long test.
 * Readers can also take a short check of the basics to tick what they already know.
 */
enum class Placement(val stage: Int, @StringRes val titleRes: Int, @StringRes val descriptionRes: Int) {
    NEW(0, R.string.learning_place_new, R.string.learning_place_new_text),
    READER(1, R.string.learning_place_reader, R.string.learning_place_reader_text),
    RECITER(3, R.string.learning_place_reciter, R.string.learning_place_reciter_text),
}

/** The learner's start and goal, kept on the device. */
object LearningPreferences {
    const val NOT_CHOSEN = -1

    private val KEY_START_STAGE = PrefKey(intPreferencesKey("learning_start_stage"), NOT_CHOSEN)

    /** [NOT_CHOSEN] until the learner picks a start. */
    fun startStage(): Flow<Int> = DataStoreManager.flow(KEY_START_STAGE)

    suspend fun setStartStage(stage: Int) = DataStoreManager.write(KEY_START_STAGE, stage)

    /** No goal surah chosen. */
    const val NO_GOAL = 0

    private val KEY_GOAL_SURAH = PrefKey(intPreferencesKey("learning_goal_surah"), NO_GOAL)

    /** The surah the learner aims to understand (journey 4), or [NO_GOAL]. */
    fun goalSurah(): Flow<Int> = DataStoreManager.flow(KEY_GOAL_SURAH)

    suspend fun setGoalSurah(surahNo: Int) = DataStoreManager.write(KEY_GOAL_SURAH, surahNo)

    /** The choices for new words a day; 0 turns them off. The design's default is 10. */
    val NEW_WORDS_CHOICES = listOf(0, 5, 10, 20)

    private val KEY_NEW_WORDS_PER_DAY = PrefKey(intPreferencesKey("learning_new_words_per_day"), 10)

    fun newWordsPerDay(): Flow<Int> = DataStoreManager.flow(KEY_NEW_WORDS_PER_DAY)

    suspend fun setNewWordsPerDay(count: Int) = DataStoreManager.write(KEY_NEW_WORDS_PER_DAY, count)

    private val KEY_REMINDER = PrefKey(booleanPreferencesKey("learning_reminder"), false)

    /** The opt-in reminder when reviews are due. Off until the learner turns it on. */
    fun reminderEnabled(): Flow<Boolean> = DataStoreManager.flow(KEY_REMINDER)

    suspend fun setReminderEnabled(enabled: Boolean) = DataStoreManager.write(KEY_REMINDER, enabled)

    /** Forgets the start, goal and daily words, as if learning mode was never opened. */
    suspend fun reset() {
        setStartStage(NOT_CHOSEN)
        setGoalSurah(NO_GOAL)
        DataStoreManager.remove(KEY_NEW_WORDS_PER_DAY)
    }
}
