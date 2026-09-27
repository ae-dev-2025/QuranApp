package com.quranapp.android.learning.ui

import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import com.quranapp.android.activities.base.BaseActivity
import com.quranapp.android.compose.theme.QuranAppTheme

/** The Learn tab, opened from the bottom bar like Search. */
class ActivityLearn : BaseActivity() {
    override fun getLayoutResource() = 0

    override fun onActivityInflated(activityView: View, savedInstanceState: Bundle?) {
        setContent {
            QuranAppTheme {
                LearnScreen()
            }
        }
    }
}
