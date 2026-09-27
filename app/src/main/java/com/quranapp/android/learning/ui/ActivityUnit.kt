package com.quranapp.android.learning.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import com.quranapp.android.activities.base.BaseActivity
import com.quranapp.android.compose.theme.QuranAppTheme
import com.quranapp.android.learning.path.Layer

/** A surah unit of the path. Open it with [intent]. */
class ActivityUnit : BaseActivity() {
    override fun getLayoutResource() = 0

    override fun onActivityInflated(activityView: View, savedInstanceState: Bundle?) {
        val surahNo = intent.getIntExtra(EXTRA_SURAH_NO, 0)
        if (surahNo !in 1..114) {
            finish()
            return
        }

        val focus = intent.getStringExtra(EXTRA_FOCUS)?.let { name -> Layer.entries.firstOrNull { it.name == name } }

        setContent {
            QuranAppTheme {
                UnitScreen(surahNo, focus)
            }
        }
    }

    companion object {
        private const val EXTRA_SURAH_NO = "surah_no"
        private const val EXTRA_FOCUS = "focus"

        /** [focus] opens that layer's step first, e.g. Words from a goal. */
        fun intent(context: Context, surahNo: Int, focus: Layer? = null): Intent =
            Intent(context, ActivityUnit::class.java)
                .putExtra(EXTRA_SURAH_NO, surahNo)
                .putExtra(EXTRA_FOCUS, focus?.name)
    }
}
