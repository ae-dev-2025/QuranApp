"""Builds learning_pack.db from the pinned sources and the app's Quran text.

    python -m learning_pack.build --data DIR --quran-db PATH --out learning_pack.db --version 1

DIR holds the files listed in README.md. PATH is app/src/main/assets/db/quranapp.db.
"""

import argparse
import sys
from pathlib import Path

from . import align, corpus, lexicon, pack, sources
from .buckwalter import to_app, to_unicode


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--data", type=Path, required=True)
    parser.add_argument("--quran-db", type=Path, required=True)
    parser.add_argument("--out", type=Path, required=True)
    parser.add_argument("--version", type=int, required=True, help="the pack's version, 1, 2, ...")
    args = parser.parse_args(argv)

    corpus_path = sources.verify(args.data / sources.CORPUS.file_name, sources.CORPUS)
    words = corpus.read_words(corpus_path)
    aligned = align.align(words, align.load_app_words(args.quran_db), to_unicode)
    roots, lemmas = lexicon.build(words)

    meta = {
        "pack_version": str(args.version),
        "schema_version": str(pack.SCHEMA_VERSION),
        "corpus_sha256": sources.CORPUS.sha256,
        "words": str(len(aligned)),
    }
    pack.write(args.out, aligned, roots, lemmas, [sources.CORPUS], meta, to_app)

    print(f"{args.out}: {len(aligned)} words, {len(lemmas)} lemmas, {len(roots)} roots")
    print(f"size {args.out.stat().st_size:,} bytes, SHA-256 {sources.sha256_of(args.out)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
