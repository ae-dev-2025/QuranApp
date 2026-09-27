"""The external data the pack is built from, pinned by SHA-256.

The raw files are not stored in the repository. README.md explains where to get them.
A file with a different hash is refused, so a pack can only be built from the exact
data that was reviewed.
"""

import hashlib
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class Source:
    key: str
    name: str
    file_name: str
    sha256: str
    licence: str
    url: str
    notice: str


CORPUS = Source(
    key="corpus",
    name="Quranic Arabic Corpus 0.4",
    file_name="quranic-corpus-morphology-0.4.txt",
    sha256="a1d12923815341face765083805d2148ed2d9f5cc3f7d6665219d887675d8c46",
    licence="GNU General Public License",
    url="https://corpus.quran.com",
    notice=(
        "Quranic Arabic Corpus (Version 0.4), Copyright (C) 2011 Kais Dukes. "
        "License: GNU General Public License. Syntactic and morphological annotation "
        "of the Quran, building on the verified Arabic text distributed by the Tanzil "
        "project. Check updates at http://corpus.quran.com/download"
    ),
)

MASAQ = Source(
    key="masaq",
    name="MASAQ: Morphologically-Analyzed and Syntactically-Annotated Quran, version 5",
    file_name="MASAQ.tsv",
    sha256="aac224f1b852a1a87e5a896b76c4b55df7c29369a7da836aea1b7286a9c3a931",
    licence="Creative Commons Attribution 4.0 International (CC BY 4.0)",
    url="https://doi.org/10.17632/9yvrzxktmr.5",
    notice=(
        "Sawalha, M., Yagi, S., Alshargi, F., Hammo, B., Alshdaifat, A. (2024). "
        "MASAQ: Morphologically-Analyzed and Syntactically-Annotated Quran Dataset, "
        "Mendeley Data, V5, doi:10.17632/9yvrzxktmr.5. Licensed under CC BY 4.0: "
        "https://creativecommons.org/licenses/by/4.0/. The pack reformats and aligns it."
    ),
)


class SourceMismatch(Exception):
    pass


def sha256_of(path: Path) -> str:
    digest = hashlib.sha256()
    with open(path, "rb") as file:
        for block in iter(lambda: file.read(1 << 20), b""):
            digest.update(block)
    return digest.hexdigest()


def verify(path: Path, source: Source) -> Path:
    """Returns the path if the file is exactly the pinned source, raises otherwise."""
    actual = sha256_of(path)
    if actual != source.sha256:
        raise SourceMismatch(
            f"{path} is not {source.name}: expected SHA-256 {source.sha256}, got {actual}"
        )
    return path
