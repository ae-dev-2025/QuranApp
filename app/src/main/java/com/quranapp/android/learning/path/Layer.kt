package com.quranapp.android.learning.path

import androidx.annotation.StringRes
import com.quranapp.android.R
import com.quranapp.android.learning.concepts.Track

/**
 * The layers of understanding an ayah (decision 3). Read and Recite are concepts found in
 * the text; Words are dictionary words from the learning pack. Grammar is added in
 * milestone 8.
 */
enum class Layer(@StringRes val labelRes: Int, val track: Track?) {
    READ(R.string.learning_layer_read, Track.READING),
    RECITE(R.string.learning_layer_recite, Track.TAJWEED),
    WORDS(R.string.learning_layer_words, null),
}
