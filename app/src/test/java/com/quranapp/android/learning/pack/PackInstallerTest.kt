package com.quranapp.android.learning.pack

import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import java.util.zip.GZIPOutputStream

class PackInstallerTest {
    private val folder: File = Files.createTempDirectory("pack-installer").toFile()
    private val pack = "a tiny learning pack".toByteArray()
    private val target = File(folder, "learning/learning_pack-v1.db")

    @After
    fun cleanUp() {
        folder.deleteRecursively()
    }

    @Test
    fun copyHashed_returnsTheSha256OfTheBytes() {
        val output = ByteArrayOutputStream()
        val hash = PackInstaller.copyHashed(ByteArrayInputStream("abc".toByteArray()), output, maxBytes = 3)
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", hash)
        assertEquals("abc", output.toString())
    }

    @Test
    fun copyHashed_stopsAtTheLimit() {
        assertThrowsIntegrity {
            PackInstaller.copyHashed(ByteArrayInputStream(ByteArray(10)), ByteArrayOutputStream(), maxBytes = 9)
        }
    }

    @Test
    fun install_putsTheCheckedPackInPlace() {
        val download = gzip(pack)
        install(download)

        assertArrayEquals(pack, target.readBytes())
        assertFalse("the download is deleted", download.exists())
        assertFalse("no temporary file is left", File(target.path + ".new").exists())
    }

    @Test
    fun install_refusesAChangedDownload() {
        val download = gzip(pack)
        assertThrowsIntegrity { install(download, downloadSha256 = sha256("something else".toByteArray())) }

        assertFalse(target.exists())
        assertFalse(download.exists())
    }

    @Test
    fun install_refusesAPackThatDoesNotMatch_andKeepsTheOldOne() {
        target.parentFile!!.mkdirs()
        target.writeText("old pack")
        assertThrowsIntegrity { install(gzip(pack), packSha256 = sha256("another pack".toByteArray())) }

        assertEquals("old pack", target.readText())
    }

    @Test
    fun install_refusesAPackBiggerThanExpected() {
        assertThrowsIntegrity { install(gzip(pack), packBytes = pack.size - 1L) }
        assertFalse(target.exists())
    }

    @Test
    fun install_replacesAnOlderPack() {
        target.parentFile!!.mkdirs()
        target.writeText("old pack")
        install(gzip(pack))
        assertTrue(target.readBytes().contentEquals(pack))
    }

    private fun install(
        download: File,
        downloadSha256: String = sha256(download.readBytes()),
        packBytes: Long = pack.size.toLong(),
        packSha256: String = sha256(pack),
    ) = PackInstaller.install(download, target, downloadSha256, packBytes, packSha256)

    private fun gzip(bytes: ByteArray): File {
        val file = File(folder, "download.gz")
        GZIPOutputStream(file.outputStream()).use { it.write(bytes) }
        return file
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    private fun assertThrowsIntegrity(block: () -> Unit) {
        try {
            block()
            fail("expected PackIntegrityException")
        } catch (expected: PackIntegrityException) {
            // the file was refused
        }
    }
}
