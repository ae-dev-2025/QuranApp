package com.quranapp.android.learning.ui

import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.quranapp.android.activities.base.BaseActivity

/** The whole path, opened from the Learn tab's "Your path" card. */
class ActivityPath : BaseActivity() {
    private val viewModel: PathViewModel by viewModels()

    override fun getLayoutResource() = 0

    override fun onActivityInflated(activityView: View, savedInstanceState: Bundle?) {
        setContent {
            LearningTheme {
                // Back to the Learn tab, which shows the choices again.
                PathScreen(viewModel, onChangeStart = { viewModel.changeStart(onSaved = ::finish) })
            }
        }
    }
}
