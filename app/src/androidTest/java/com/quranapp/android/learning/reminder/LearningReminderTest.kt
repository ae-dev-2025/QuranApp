package com.quranapp.android.learning.reminder

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.path.LearningPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** The reminder's rule on the device's own data: it speaks only when it's on and something is due. */
@RunWith(AndroidJUnit4::class)
class LearningReminderTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun postsOnlyWhenOnAndSomethingIsDue() = runBlocking {
        val wasOn = LearningPreferences.reminderEnabled().first()
        val farFuture = System.currentTimeMillis() + 3650L * 86_400_000L // every card is due by then
        try {
            LearningPreferences.setReminderEnabled(false)
            assertFalse("off: never", LearningReminder.notifyIfDue(context, now = farFuture))

            LearningPreferences.setReminderEnabled(true)
            assertFalse("nothing is due before 1971", LearningReminder.notifyIfDue(context, now = 0))

            assumeTrue("this device has no review cards", DatabaseProvider.getUserDatabase(context).reviewDao().dueCount(farFuture) > 0)
            assertTrue(LearningReminder.notifyIfDue(context, now = farFuture))
        } finally {
            LearningPreferences.setReminderEnabled(wasOn)
        }
    }
}
