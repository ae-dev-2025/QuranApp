package com.quranapp.android.learning.pack

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.util.zip.GZIPInputStream

/** A file that isn't exactly the expected pack. Nothing is installed when this is thrown. */
class PackIntegrityException(message: String) : IOException(message)

/**
 * Checks and installs a downloaded pack. Plain Java I/O, no Android, so it runs in unit tests.
 *
 * Safety rules:
 * - every byte is hashed while it is copied, and a file is only used if its SHA-256 matches;
 * - copying stops as soon as a file is bigger than expected, so a bad download can't fill
 *   the phone's storage;
 * - the pack is unzipped next to its final place and only then renamed into place, so the
 *   installed pack is either the old one or the complete new one, never half written.
 */
object PackInstaller {
    private const val BUFFER_BYTES = 64 * 1024

    /** An output that throws the bytes away (OutputStream.nullOutputStream needs Android 13). */
    private object Discard : OutputStream() {
        override fun write(b: Int) = Unit
        override fun write(b: ByteArray, off: Int, len: Int) = Unit
    }

    /**
     * Copies [input] to [output], calling [onProgress] with the bytes copied so far.
     * Returns the SHA-256 of what was copied, in lowercase hex.
     */
    fun copyHashed(
        input: InputStream,
        output: OutputStream,
        maxBytes: Long,
        onProgress: (Long) -> Unit = {},
    ): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(BUFFER_BYTES)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            if (total > maxBytes) throw PackIntegrityException("more than $maxBytes bytes")
            digest.update(buffer, 0, read)
            output.write(buffer, 0, read)
            onProgress(total)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Checks [download] (the .gz file) against the release, unzips it, checks the pack, and
     * moves it to [target]. [download] is deleted either way.
     */
    fun install(
        download: File,
        target: File,
        downloadSha256: String = LearningPackRelease.DOWNLOAD_SHA256,
        packBytes: Long = LearningPackRelease.PACK_BYTES,
        packSha256: String = LearningPackRelease.PACK_SHA256,
    ) {
        val unzipped = File(target.parentFile, target.name + ".new")
        try {
            val downloadHash = download.inputStream().use { input ->
                copyHashed(input, Discard, maxBytes = download.length())
            }
            if (downloadHash != downloadSha256) {
                throw PackIntegrityException("download SHA-256 is $downloadHash, expected $downloadSha256")
            }

            target.parentFile?.mkdirs()
            val packHash = GZIPInputStream(download.inputStream()).use { input ->
                unzipped.outputStream().use { output -> copyHashed(input, output, maxBytes = packBytes) }
            }
            if (packHash != packSha256) {
                throw PackIntegrityException("pack SHA-256 is $packHash, expected $packSha256")
            }

            if (!unzipped.renameTo(target)) {
                // renameTo won't replace an existing file on some systems: remove the old one first.
                target.delete()
                if (!unzipped.renameTo(target)) throw IOException("could not move the pack into place")
            }
        } finally {
            unzipped.delete()
            download.delete()
        }
    }
}
