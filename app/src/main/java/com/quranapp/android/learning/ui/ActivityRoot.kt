package com.quranapp.android.learning.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import com.quranapp.android.activities.base.BaseActivity
import com.quranapp.android.compose.theme.QuranAppTheme

/** The page of one root, such as ع ب د. Open it with [ActivityRoot.intent]. */
class ActivityRoot : BaseActivity() {
    override fun getLayoutResource() = 0

    override fun onActivityInflated(activityView: View, savedInstanceState: Bundle?) {
        val rootKey = intent.getStringExtra(EXTRA_ROOT_KEY)
        if (rootKey == null) {
            finish()
            return
        }

        setContent {
            QuranAppTheme {
                RootScreen(rootKey)
            }
        }
    }

    companion object {
        private const val EXTRA_ROOT_KEY = "root_key"

        /** [rootKey] is the pack's stable key, such as `Ebd`, not the numeric id. */
        fun intent(context: Context, rootKey: String): Intent =
            Intent(context, ActivityRoot::class.java).putExtra(EXTRA_ROOT_KEY, rootKey)
    }
}
