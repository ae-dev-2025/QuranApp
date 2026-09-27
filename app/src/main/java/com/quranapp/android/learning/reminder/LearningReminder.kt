package com.quranapp.android.learning.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.quranapp.android.R
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.path.LearningPreferences
import com.quranapp.android.learning.practice.DailyReview
import com.quranapp.android.learning.ui.ActivityLearn
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * The opt-in learning reminder (decision 10): once a day, and only when reviews are due.
 * With nothing due there's nothing to say, so it stays quiet.
 */
object LearningReminder {
    const val CHANNEL_ID = "learning_reminder"
    private const val WORK_ID = "learning_reminder"
    private const val NOTIFICATION_ID = 0x0010

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<LearningReminderWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_ID, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_ID)
    }

    /** Called with the app's other channels at start-up; its name and description are translatable. */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.learning_reminder_channel),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = context.getString(R.string.learning_reminder_channel_text) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Posts the reminder if it's on and something is due. Returns whether it posted. */
    suspend fun notifyIfDue(context: Context, now: Long = System.currentTimeMillis()): Boolean {
        if (!LearningPreferences.reminderEnabled().first()) return false
        val due = DatabaseProvider.getUserDatabase(context).reviewDao().dueCount(now)
        if (due == 0) return false
        val manager = ContextCompat.getSystemService(context, NotificationManager::class.java) ?: return false

        val openLearn = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            Intent(context, ActivityLearn::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val minutes = DailyReview.minutesFor(due.coerceAtMost(DailyReview.SESSION_SIZE), newWords = 0)
        val text = context.resources.getQuantityString(R.plurals.learning_reviews_due, due, due) + " · " +
            context.getString(R.string.learning_about_minutes, minutes)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.learning_reminder_title))
            .setContentText(text)
            .setSmallIcon(R.drawable.dr_logo)
            .setContentIntent(openLearn)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
        return true
    }
}

class LearningReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        LearningReminder.notifyIfDue(applicationContext)
        return Result.success()
    }
}
