package com.quranapp.android.learning.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quranapp.android.R
import com.quranapp.android.learning.words.AyahWord
import com.quranapp.android.learning.words.SentenceRoles
import com.quranapp.android.learning.words.WordRole

/** The Grammar tab shows the ayah's concepts, or each word's role in the sentence. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GrammarViewSwitch(byWord: Boolean, onChange: (Boolean) -> Unit) {
    val options = listOf(R.string.learning_grammar_view_concepts, R.string.learning_grammar_view_words)
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                selected = byWord == (index == 1),
                onClick = { onChange(index == 1) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) { Text(stringResource(label)) }
        }
    }
}

/**
 * Each word of the ayah with its role (iʿrāb). It is one analysis, so it says so, and points
 * to the tafsir, where scholars discuss the ayah (design: "Scholars differ on iʿrāb").
 */
internal fun LazyListScope.wordRoleItems(
    words: List<AyahWord>,
    texts: List<String>,
    arabicFont: FontFamily,
    onOpenTafsir: () -> Unit,
) {
    item(key = "roles-note") {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(stringResource(R.string.learning_roles_note), style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
            TextButton(onClick = onOpenTafsir) { Text(stringResource(R.string.learning_roles_open_tafsir)) }
        }
    }
    for (word in words) {
        item(key = "roles-${word.wordIndex}") {
            WordRolesRow(texts.getOrNull(word.wordIndex).orEmpty(), word, arabicFont)
        }
    }
}

@Composable
private fun WordRolesRow(text: String, word: AyahWord, arabicFont: FontFamily) {
    val roles = SentenceRoles.of(word.syntax)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            word.gloss?.let { Text(it, style = typography.titleSmall, fontWeight = FontWeight.SemiBold) }
            if (roles.isEmpty()) {
                Text(stringResource(R.string.learning_roles_none), style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
            }
            for (role in roles) {
                Text(roleLine(role), style = typography.bodySmall)
            }
        }
        Text(text, fontFamily = arabicFont, style = typography.titleLarge, color = colorScheme.primary)
    }
}

/** "preposition · حَرۡف جَرّ (ل)" or "subject · مُبۡتَدَأ · in rafʿ, ḍamma". */
@Composable
private fun roleLine(role: WordRole): String {
    val name = stringResource(role.role.nameRes)
    val term = role.role.arabic + (role.part?.let { " ($it)" } ?: "")
    val case = role.case?.let { case ->
        val caseName = stringResource(case.nameRes)
        when {
            role.fixedEnding -> stringResource(R.string.learning_role_case_place, caseName)
            role.sign != null -> stringResource(R.string.learning_role_case_sign, caseName, stringResource(role.sign.nameRes))
            else -> stringResource(R.string.learning_role_case, caseName)
        }
    }
    return arabicExamplesInOrder(listOfNotNull(name, term, case).joinToString(" · "))
}
