package com.quranapp.android.learning.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import com.quranapp.android.activities.base.BaseActivity
import com.quranapp.android.learning.concepts.ConceptCatalog

/**
 * The learning page of one concept. Open it with [ActivityConcept.intent], so the caller
 * never has to know the name of the extra.
 */
class ActivityConcept : BaseActivity() {
    override fun getLayoutResource() = 0

    override fun onActivityInflated(activityView: View, savedInstanceState: Bundle?) {
        val concept = intent.getStringExtra(EXTRA_CONCEPT_ID)?.let { ConceptCatalog[it] }

        // Unknown or missing ID (e.g. a concept removed in a later version): nothing to show.
        if (concept == null) {
            finish()
            return
        }

        setContent {
            LearningTheme {
                ConceptScreen(concept)
            }
        }
    }

    companion object {
        private const val EXTRA_CONCEPT_ID = "concept_id"

        fun intent(context: Context, conceptId: String): Intent =
            Intent(context, ActivityConcept::class.java).putExtra(EXTRA_CONCEPT_ID, conceptId)
    }
}
