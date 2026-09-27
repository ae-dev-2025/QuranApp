package com.quranapp.android.learning.ui

import android.widget.Toast
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quranapp.android.R
import com.quranapp.android.utils.mediaplayer.WbwAudioPlayResult
import com.quranapp.android.utils.mediaplayer.WbwAudioPlayer
import com.quranapp.android.utils.univ.MessageUtils
import kotlinx.coroutines.launch

/**
 * "Hear it": plays one word of the Quran with the app's word-by-word audio, the same player
 * and messages as the reader. There are no recordings of single letters yet (see the plan),
 * so lessons play real words, as the design suggests.
 */
@Composable
internal fun HearWordButton(surahNo: Int, ayahNo: Int, wordIndex: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            if (loading) return@IconButton
            loading = true
            scope.launch {
                try {
                    when (WbwAudioPlayer.play(context, surahNo, ayahNo, wordIndex)) {
                        WbwAudioPlayResult.Success -> Unit
                        WbwAudioPlayResult.NoInternet -> MessageUtils.popNoInternetToast(context)
                        WbwAudioPlayResult.TimingsNotLoaded ->
                            MessageUtils.showRemovableToast(context, R.string.wbwAudioTimingsCouldNotLoad, Toast.LENGTH_LONG)
                        WbwAudioPlayResult.InvalidTiming, WbwAudioPlayResult.NoChapterAudio ->
                            MessageUtils.showRemovableToast(context, R.string.wbwAudioCouldNotPlay, Toast.LENGTH_LONG)
                    }
                } finally {
                    loading = false
                }
            }
        },
        modifier = modifier,
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Icon(painterResource(R.drawable.ic_play), contentDescription = stringResource(R.string.playWord), tint = colorScheme.primary)
        }
    }
}
