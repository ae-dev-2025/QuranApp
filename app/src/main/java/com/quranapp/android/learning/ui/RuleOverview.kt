package com.quranapp.android.learning.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.quranapp.android.R
import com.quranapp.android.compose.theme.alpha
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.concepts.Concept
import com.quranapp.android.learning.concepts.ConceptIds
import com.quranapp.android.learning.lessons.LessonCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** What decides each rule, shown under its name. */
private val TRIGGERS: Map<String, Int> = mapOf(
    ConceptIds.IZHAR to R.string.learning_rule_before_izhar,
    ConceptIds.IDGHAM_GHUNNAH to R.string.learning_rule_before_idgham_ghunnah,
    ConceptIds.IDGHAM_NO_GHUNNAH to R.string.learning_rule_before_idgham_no_ghunnah,
    ConceptIds.IQLAB to R.string.learning_rule_before_iqlab,
    ConceptIds.IKHFA to R.string.learning_rule_before_ikhfa,
    ConceptIds.IZHAR_SHAFAWI to R.string.learning_rule_before_izhar_shafawi,
    ConceptIds.IDGHAM_SHAFAWI to R.string.learning_rule_before_idgham_shafawi,
    ConceptIds.IKHFA_SHAFAWI to R.string.learning_rule_before_ikhfa_shafawi,
)

/**
 * The page body of an umbrella concept such as noon sākinah: its rules side by side, each
 * with what triggers it and one short example (the rule's own key example), so learners can
 * tell them apart. Tapping a rule opens its page.
 */
@Composable
fun RuleOverview(rules: List<Concept>, onOpen: (Concept) -> Unit) {
    val context = LocalContext.current
    val repository = remember { DatabaseProvider.getQuranRepository(context) }

    // concept ID -> the Arabic of its key example; empty until loaded.
    val examples by produceState<Map<String, String>>(initialValue = emptyMap(), rules) {
        value = withContext(Dispatchers.IO) {
            rules.mapNotNull { rule ->
                val example = LessonCatalog[rule.id]?.keyExample ?: return@mapNotNull null
                rule.id to repository.keyExampleArabic(example)
            }.toMap()
        }
    }

    val arabicFont = remember { FontFamily(Font(R.font.uthmanic_hafs)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface),
    ) {
        rules.forEachIndexed { index, rule ->
            if (index > 0) HorizontalDivider(color = colorScheme.outlineVariant)
            RuleRow(rule, TRIGGERS[rule.id], examples[rule.id], arabicFont) { onOpen(rule) }
        }
    }
}

@Composable
private fun RuleRow(
    rule: Concept,
    @StringRes trigger: Int?,
    arabic: String?,
    arabicFont: FontFamily,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = stringResource(R.string.learning_open_lesson), onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(rule.titleRes), style = typography.titleSmall)
            if (trigger != null) {
                Text(
                    text = stringResource(trigger),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
            if (arabic != null) {
                Text(
                    text = arabic,
                    fontFamily = arabicFont,
                    style = typography.titleLarge,
                    color = colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(colorScheme.primary.alpha(0.12f))
                        .padding(horizontal = 6.dp),
                )
            }
        }
        OpenChevron()
    }
}
