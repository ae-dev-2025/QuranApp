package com.quranapp.android.learning.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import com.quranapp.android.activities.base.BaseActivity
import com.quranapp.android.compose.theme.QuranAppTheme

/** A surah unit of the path. Open it with [intent]. */
class ActivityUnit : BaseActivity() {
    override fun getLayoutResource() = 0

    override fun onActivityInflated(activityView: View, savedInstanceState: Bundle?) {
        val surahNo = intent.getIntExtra(EXTRA_SURAH_NO, 0)
        if (surahNo !in 1..114) {
            finish()
            return
        }

        setContent {
            QuranAppTheme {
                UnitScreen(surahNo)
            }
        }
    }

    companion object {
        private const val EXTRA_SURAH_NO = "surah_no"

        fun intent(context: Context, surahNo: Int): Intent =
            Intent(context, ActivityUnit::class.java).putExtra(EXTRA_SURAH_NO, surahNo)
    }
}
