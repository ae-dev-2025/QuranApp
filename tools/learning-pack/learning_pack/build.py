"""Builds learning_pack.db from the pinned sources and the app's Quran text.

    python -m learning_pack.build --data DIR --quran-db PATH --out learning_pack.db --version 1

DIR holds the files listed in README.md. PATH is app/src/main/assets/db/quranapp.db.
"""

import argparse
import gzip
import shutil
import sys
from pathlib import Path

from . import align, corpus, lexicon, masaq, pack, sources
from .buckwalter import to_app, to_unicode

# MASAQ leaves out 21 words (mostly مَا in أَيۡنَ مَا, which everyday spelling joins into
# one word). More than this means something changed and needs a look.
MAX_WORDS_WITHOUT_SENTENCE_DATA = 25


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--data", type=Path, required=True)
    parser.add_argument("--quran-db", type=Path, required=True)
    parser.add_argument("--out", type=Path, required=True)
    parser.add_argument("--version", type=int, required=True, help="the pack's version, 1, 2, ...")
    args = parser.parse_args(argv)

    corpus_path = sources.verify(args.data / sources.CORPUS.file_name, sources.CORPUS)
    words = corpus.read_words(corpus_path)
    app_words = align.load_app_words(args.quran_db)
    aligned = align.align(words, app_words, to_unicode)
    roots, lemmas = lexicon.build(words)

    masaq_path = sources.verify(args.data / sources.MASAQ.file_name, sources.MASAQ)
    sentence = masaq.align(masaq.read_words(masaq_path), app_words)
    if len(sentence.unmatched_app_words) > MAX_WORDS_WITHOUT_SENTENCE_DATA:
        raise SystemExit(f"{len(sentence.unmatched_app_words)} words have no MASAQ data: {sentence.unmatched_app_words[:20]}")

    meta = {
        "pack_version": str(args.version),
        "schema_version": str(pack.SCHEMA_VERSION),
        "corpus_sha256": sources.CORPUS.sha256,
        "masaq_sha256": sources.MASAQ.sha256,
        "words": str(len(aligned)),
    }
    pack.write(args.out, aligned, roots, lemmas, [sources.CORPUS, sources.MASAQ], meta, to_app,
               sentence.words, sentence.regular_ayahs)

    print(f"{args.out}: {len(aligned)} words, {len(lemmas)} lemmas, {len(roots)} roots")
    print(f"sentence data for {len(sentence.words)} words; none for {len(sentence.unmatched_app_words)}; "
          f"glosses for {len(sentence.regular_ayahs)} ayahs")
    print(f"size {args.out.stat().st_size:,} bytes, SHA-256 {sources.sha256_of(args.out)}")

    download = write_gzip(args.out)
    print(f"{download}: {download.stat().st_size:,} bytes, SHA-256 {sources.sha256_of(download)}")
    return 0


def write_gzip(pack_path: Path) -> Path:
    """The file to publish. A fixed name and time inside the gzip keep it reproducible."""
    target = pack_path.with_name(pack_path.name + ".gz")
    with open(pack_path, "rb") as source, open(target, "wb") as raw:
        with gzip.GzipFile(filename="learning_pack.db", mode="wb", fileobj=raw, mtime=0, compresslevel=9) as out:
            shutil.copyfileobj(source, out)
    return target


if __name__ == "__main__":
    sys.exit(main())
