package com.quranapp.android.learning.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.quranapp.android.activities.base.BaseActivity
import com.quranapp.android.compose.theme.QuranAppTheme

/** A practice session: a check of some items, or the daily review. Open with [intent]. */
class ActivityPractice : BaseActivity() {
    private val viewModel: PracticeViewModel by viewModels()

    override fun getLayoutResource() = 0

    override fun onActivityInflated(activityView: View, savedInstanceState: Bundle?) {
        val itemIds = intent.getStringArrayListExtra(EXTRA_ITEM_IDS).orEmpty()
        val mode = intent.getStringExtra(EXTRA_MODE)?.let { runCatching { PracticeMode.valueOf(it) }.getOrNull() }
        if (itemIds.isEmpty() || mode == null) {
            finish()
            return
        }
        viewModel.start(itemIds, mode)

        setContent {
            QuranAppTheme {
                PracticeScreen(viewModel, onClose = ::finish)
            }
        }
    }

    companion object {
        private const val EXTRA_ITEM_IDS = "item_ids"
        private const val EXTRA_MODE = "mode"

        fun intent(context: Context, itemIds: List<String>, mode: PracticeMode): Intent =
            Intent(context, ActivityPractice::class.java)
                .putStringArrayListExtra(EXTRA_ITEM_IDS, ArrayList(itemIds))
                .putExtra(EXTRA_MODE, mode.name)
    }
}
