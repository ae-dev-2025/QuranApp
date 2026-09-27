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
    const val VERSION = 1

    const val URL =
        "https://github.com/ae-dev-2025/QuranApp/releases/download/learning-pack-v1/learning_pack-v1.db.gz"

    /** The gzip file as downloaded. */
    const val DOWNLOAD_BYTES = 8_018_018L
    const val DOWNLOAD_SHA256 = "27c5d349a89a3ac26a9aeb2912dc95710eb5b54b8e23efed4b210da3fa0676a8"

    /** learning_pack.db once unzipped. */
    const val PACK_BYTES = 25_485_312L
    const val PACK_SHA256 = "66ae1b5ad5e14083d2b44a78279d6facd308dd3e21216d89aae45fef72e2bc5a"
}
