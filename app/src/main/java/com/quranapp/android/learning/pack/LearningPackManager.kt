package com.quranapp.android.learning.pack

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/** Where the learning pack is, as the UI shows it. */
sealed interface LearningPackState {
    data object NotInstalled : LearningPackState
    data class Downloading(val bytes: Long, val total: Long) : LearningPackState
    data object Installing : LearningPackState
    data object Installed : LearningPackState
    data class Failed(val reason: String) : LearningPackState
}

/**
 * Downloads, installs, opens and deletes the learning pack (decision 13: an optional download).
 *
 * The download keeps going if the learner leaves the screen, because it runs in this
 * object's own scope. If the app is closed it simply starts again next time.
 */
object LearningPackManager {
    private val _state = MutableStateFlow<LearningPackState>(LearningPackState.NotInstalled)
    val state: StateFlow<LearningPackState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val databaseLock = Mutex()
    private var database: LearningPackDatabase? = null
    private var download: Job? = null

    /**
     * HTTPS only, with modern TLS, and never follows a redirect from HTTPS to plain HTTP.
     * (The app's shared client also allows plain HTTP for other servers.)
     */
    private val client by lazy {
        OkHttpClient.Builder()
            .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS))
            .followSslRedirects(false)
            .build()
    }

    /** The installed pack. The version is in the name, so an app update never opens an old pack. */
    fun file(context: Context): File =
        File(context.filesDir, "learning/learning_pack-v${LearningPackRelease.VERSION}.db")

    /** Looks at what's on disk; call once when a screen that uses the pack appears. */
    fun refresh(context: Context) {
        if (_state.value is LearningPackState.Downloading || _state.value == LearningPackState.Installing) return
        deleteOtherVersions(context)
        _state.value = if (file(context).exists()) LearningPackState.Installed else LearningPackState.NotInstalled
    }

    /** The pack's database, or null if it isn't installed. Opened once and kept open. */
    suspend fun database(context: Context): LearningPackDatabase? = databaseLock.withLock {
        database ?: file(context).takeIf { it.exists() }
            ?.let { LearningPackDatabase.open(context, it) }
            ?.also { database = it }
    }

    /** Starts downloading, unless a download is already running. */
    fun download(context: Context) {
        if (download?.isActive == true) return
        val appContext = context.applicationContext
        download = scope.launch {
            val job = coroutineContext.job
            val partial = File(appContext.cacheDir, "learning_pack.download")
            try {
                _state.value = LearningPackState.Downloading(0, LearningPackRelease.DOWNLOAD_BYTES)
                fetch(partial) { bytes ->
                    job.ensureActive()
                    _state.value = LearningPackState.Downloading(bytes, LearningPackRelease.DOWNLOAD_BYTES)
                }
                _state.value = LearningPackState.Installing
                databaseLock.withLock {
                    closeDatabase()
                    PackInstaller.install(partial, file(appContext))
                }
                _state.value = LearningPackState.Installed
            } catch (cancelled: CancellationException) {
                partial.delete()
                refresh(appContext)
                throw cancelled
            } catch (error: Exception) {
                // No connection, a changed file, a full disk…: nothing was installed, so the
                // learner can simply try again.
                partial.delete()
                _state.value = LearningPackState.Failed(error.message ?: error.javaClass.simpleName)
            }
        }
    }

    fun cancelDownload() {
        download?.cancel()
    }

    suspend fun delete(context: Context) {
        databaseLock.withLock {
            closeDatabase()
            file(context).parentFile?.deleteRecursively()
        }
        _state.value = LearningPackState.NotInstalled
    }

    /** Closes the database, for example before the user's data is restored from a backup. */
    fun close() {
        closeDatabase()
    }

    private fun fetch(target: File, onProgress: (Long) -> Unit) {
        val request = Request.Builder().url(LearningPackRelease.URL).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("the server answered ${response.code}")
            val body = response.body ?: throw IOException("the server sent nothing")
            target.outputStream().use { output ->
                PackInstaller.copyHashed(body.byteStream(), output, LearningPackRelease.DOWNLOAD_BYTES, onProgress)
            }
        }
    }

    private fun closeDatabase() {
        database?.close()
        database = null
    }

    private fun deleteOtherVersions(context: Context) {
        val current = file(context)
        current.parentFile?.listFiles()?.forEach { other ->
            if (!other.name.startsWith(current.name)) other.delete()
        }
    }
}
