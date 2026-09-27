"""Reads the Quranic Arabic Corpus morphology file into words and their segments.

Each line of the file is one *segment* of a word: a prefix (such as وَ "and"), a stem, or
a suffix (such as an attached pronoun). For example the first word of the Quran, بِسۡمِ, is
two lines:

    (1:1:1:1)  bi    P  PREFIX|bi+
    (1:1:1:2)  somi  N  STEM|POS:N|LEM:{som|ROOT:smw|M|GEN

Locations are surah:ayah:word:segment, all counted from 1. The features column holds
the segment kind (PREFIX, STEM or SUFFIX), `KEY:value` pairs (POS, LEM, ROOT, PRON, SP,
MOOD) and bare flags (M, GEN, PERF, 3MS, (IV), ...). The corpus documents them at
https://corpus.quran.com/documentation/tagset.jsp.
"""

import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Iterable, Iterator

_LOCATION = re.compile(r"^\((\d+):(\d+):(\d+):(\d+)\)$")
_KEYS = ("POS", "LEM", "ROOT", "PRON", "SP", "MOOD")
_KINDS = ("PREFIX", "STEM", "SUFFIX")


@dataclass(frozen=True)
class Segment:
    surah: int
    ayah: int
    word: int
    index: int
    form: str
    tag: str
    kind: str
    values: dict = field(hash=False, compare=True)
    flags: tuple = ()

    @property
    def lemma(self):
        return self.values.get("LEM")

    @property
    def root(self):
        return self.values.get("ROOT")


@dataclass
class Word:
    surah: int
    ayah: int
    word: int
    segments: list

    @property
    def stems(self) -> list:
        return [segment for segment in self.segments if segment.kind == "STEM"]

    @property
    def form(self) -> str:
        return "".join(segment.form for segment in self.segments)


def parse_line(line: str) -> Segment:
    # The file has Windows line endings.
    location, form, tag, features = line.rstrip("\r\n").split("\t")
    match = _LOCATION.match(location)
    if not match:
        raise ValueError(f"bad location: {location!r}")
    surah, ayah, word, index = (int(part) for part in match.groups())

    parts = features.split("|")
    kind = parts[0]
    if kind not in _KINDS:
        raise ValueError(f"bad segment kind in {line!r}")

    values = {}
    flags = []
    for part in parts[1:]:
        key, sep, value = part.partition(":")
        # Prefix flags such as "w:CONJ+" also contain a colon, but end with "+".
        if sep and key in _KEYS and not part.endswith("+"):
            values[key] = value
        else:
            flags.append(part)
    return Segment(surah, ayah, word, index, form, tag, kind, values, tuple(flags))


def parse_segments(lines: Iterable[str]) -> Iterator[Segment]:
    """Segments in file order. Comment lines, the header and blank lines are skipped."""
    for line in lines:
        if line.startswith("("):
            yield parse_line(line)


def group_words(segments: Iterable[Segment]) -> list:
    """Groups consecutive segments into words, checking that locations run in order."""
    words = []
    for segment in segments:
        key = (segment.surah, segment.ayah, segment.word)
        if words and (words[-1].surah, words[-1].ayah, words[-1].word) == key:
            expected = len(words[-1].segments) + 1
            if segment.index != expected:
                raise ValueError(f"segment {segment.index} of {key} should be {expected}")
            words[-1].segments.append(segment)
        else:
            if segment.index != 1:
                raise ValueError(f"word {key} starts at segment {segment.index}")
            words.append(Word(*key, [segment]))
    return words


def read_words(path: Path) -> list:
    with open(path, encoding="utf-8") as file:
        return group_words(parse_segments(file))
