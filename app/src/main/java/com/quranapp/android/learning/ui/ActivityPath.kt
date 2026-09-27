package com.quranapp.android.learning.ui

import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.quranapp.android.activities.base.BaseActivity
import com.quranapp.android.compose.theme.QuranAppTheme

/** The whole path, opened from the Learn tab's "Your path" card. */
class ActivityPath : BaseActivity() {
    private val viewModel: PathViewModel by viewModels()

    override fun getLayoutResource() = 0

    override fun onActivityInflated(activityView: View, savedInstanceState: Bundle?) {
        setContent {
            QuranAppTheme {
                PathScreen(viewModel)
            }
        }
    }
}
