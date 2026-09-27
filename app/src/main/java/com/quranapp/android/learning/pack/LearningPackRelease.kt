package com.quranapp.android.learning.pack

/**
 * The one learning pack this version of the app installs.
 *
 * The URL, sizes and SHA-256 hashes are fixed here, in the app's own code, so a pack is only
 * installed if it is byte for byte the file that was built and reviewed. A new pack version
 * means changing these values in a PR. See tools/learning-pack/README.md.
 */
object LearningPackRelease {
    /** The pack's `pack_version`. The schema version is [LearningPackDatabase.SCHEMA_VERSION]. */
    const val VERSION = 2

    const val URL =
        "https://github.com/ae-dev-2025/QuranApp/releases/download/learning-pack-v2/learning_pack-v2.db.gz"

    /** The gzip file as downloaded. */
    const val DOWNLOAD_BYTES = 8_063_640L
    const val DOWNLOAD_SHA256 = "3bf68f2c1fbdde478d8311a25cbc2273ce3c5ee9f24651fa90e3cb9596cad2f2"

    /** learning_pack.db once unzipped. */
    const val PACK_BYTES = 25_546_752L
    const val PACK_SHA256 = "e13617348a1c276615b19ea5171d27d5512b324eeb63e405ff26cb02d06d1ea1"
}
