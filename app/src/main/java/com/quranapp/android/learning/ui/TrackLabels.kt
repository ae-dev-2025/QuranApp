package com.quranapp.android.learning.ui

import androidx.annotation.StringRes
import com.quranapp.android.R
import com.quranapp.android.learning.concepts.Track

/** The display name of a track, e.g. "Tajweed". Used by the sheet and the concept page. */
@get:StringRes
val Track.labelRes: Int
    get() = when (this) {
        Track.READING -> R.string.learning_track_reading
        Track.TAJWEED -> R.string.learning_track_tajweed
        Track.GRAMMAR -> R.string.learning_track_grammar
    }
