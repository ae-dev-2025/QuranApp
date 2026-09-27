package com.quranapp.android.learning.path

import androidx.annotation.StringRes
import com.quranapp.android.R
import com.quranapp.android.compose.utils.preferences.DataStoreManager
import com.quranapp.android.compose.utils.preferences.PrefKey
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

/** The stage the learner chose to start at, kept on the device. */
object LearningPreferences {
    const val NOT_CHOSEN = -1

    private val KEY_START_STAGE = PrefKey(intPreferencesKey("learning_start_stage"), NOT_CHOSEN)

    /** [NOT_CHOSEN] until the learner picks a start. */
    fun startStage(): Flow<Int> = DataStoreManager.flow(KEY_START_STAGE)

    suspend fun setStartStage(stage: Int) = DataStoreManager.write(KEY_START_STAGE, stage)
}
