"""Reads MASAQ (version 5) and lines its words up with the app's words.

MASAQ gives each word's role in its sentence (iʿrāb): subject, object, the second noun of
an iḍāfa and so on, with its case or mood and the ending that shows it. It also gives an
English gloss per word. Each row is one segment of a word (prefix, stem, suffix), like the
corpus.

MASAQ is written in everyday (imlāʾī) spelling, not the Uthmani spelling of the muṣḥaf,
and in a few ayahs it splits a word in two (يَٰقَوۡمِ as يا + قوم) or leaves a word out.
So it can't be paired by position alone. Each ayah is aligned by dynamic programming over
how alike the words' letters are: a MASAQ word can match an app word, two MASAQ words can
match one app word, and a word on either side can be left unmatched. App words MASAQ
leaves out simply have no sentence data.
"""

import csv
import difflib
from dataclasses import dataclass, field
from pathlib import Path

from .align import skeleton

_SEGMENT_COLUMNS = (
    "Segmented_Word", "Morph_Tag", "Morph_Type", "Invariable_Declinable", "Syntactic_Role",
    "Possessive_Construct", "Case_Mood", "Case_Mood_Marker", "Phrase", "Phrasal_Function",
)

# Scores for the alignment. A pair must be at least this alike to count as the same word.
_MIN_SIMILARITY = 0.5
_MERGE_PENALTY = 0.1
_SKIP_SCORE = -0.3


@dataclass
class MasaqWord:
    surah: int
    ayah: int
    word: int
    text: str
    gloss: str
    segments: list = field(default_factory=list)  # dicts of _SEGMENT_COLUMNS


def read_words(path: Path) -> dict:
    """ayah_id -> the ayah's MASAQ words in order."""
    ayahs = {}
    with open(path, encoding="utf-8-sig", newline="") as file:
        for row in csv.DictReader(file, delimiter="\t"):
            surah, ayah, number = int(row["Sura_No"]), int(row["Verse_No"]), int(row["Word_No"])
            words = ayahs.setdefault(surah * 1000 + ayah, [])
            if not words or words[-1].word != number:
                words.append(MasaqWord(surah, ayah, number, row["Word"], row["Gloss"]))
            words[-1].segments.append({column: row[column] for column in _SEGMENT_COLUMNS})
    return ayahs


def _letters(text: str) -> str:
    # The two spellings differ mostly in long vowels (ضَلَٰلَةٞ / ضلالة, ٱلصَّلَوٰةِ / الصلاة),
    # so compare the other letters.
    return skeleton(text).translate(str.maketrans("", "", "ايو"))


def _ratio(a: str, b: str) -> float:
    if not a and not b:
        return 1.0
    return difflib.SequenceMatcher(None, a, b).ratio()


def similarity(app_text: str, masaq_text: str) -> float:
    """How alike two words are, ignoring long vowels (0 to 1)."""
    return _ratio(_letters(app_text), _letters(masaq_text))


def _merge_score(app_text: str, first: str, second: str):
    """The score for matching two MASAQ words to one app word, or None if they don't fit.

    Long vowels are kept here: يا has no other letters, so without them it would fit onto
    any word. The pair must match better than either word alone.
    """
    app = skeleton(app_text)
    joined = _ratio(app, skeleton(first + second))
    if joined < _MIN_SIMILARITY:
        return None
    if joined <= _ratio(app, skeleton(first)) or joined <= _ratio(app, skeleton(second)):
        return None
    return joined - _MERGE_PENALTY


def align_ayah(app_words: list, masaq_words: list) -> list:
    """For each app word, the MASAQ words that match it (none, one or two)."""
    n, m = len(app_words), len(masaq_words)
    # Almost every ayah pairs up word for word; only the others need the full search.
    if pairs_word_for_word(app_words, masaq_words):
        return [[word] for word in masaq_words]
    best = [[float("-inf")] * (m + 1) for _ in range(n + 1)]
    step = [[None] * (m + 1) for _ in range(n + 1)]
    best[0][0] = 0.0
    for i in range(n + 1):
        for j in range(m + 1):
            here = best[i][j]
            if here == float("-inf"):
                continue
            moves = []
            if i < n and j < m:
                score = similarity(app_words[i], masaq_words[j].text)
                if score >= _MIN_SIMILARITY:
                    moves.append((i + 1, j + 1, score, (i, (j,))))
            if i < n and j + 1 < m:
                score = _merge_score(app_words[i], masaq_words[j].text, masaq_words[j + 1].text)
                if score is not None:
                    moves.append((i + 1, j + 2, score, (i, (j, j + 1))))
            if i < n:
                moves.append((i + 1, j, _SKIP_SCORE, (i, ())))
            if j < m:
                moves.append((i, j + 1, _SKIP_SCORE, None))
            for ni, nj, score, match in moves:
                if here + score > best[ni][nj]:
                    best[ni][nj] = here + score
                    step[ni][nj] = (i, j, match)

    matches = [[] for _ in range(n)]
    i, j = n, m
    while (i, j) != (0, 0):
        pi, pj, match = step[i][j]
        if match is not None:
            app_index, masaq_indexes = match
            matches[app_index] = [masaq_words[k] for k in masaq_indexes]
        i, j = pi, pj
    return matches


def pairs_word_for_word(app_words: list, masaq_words: list) -> bool:
    return len(app_words) == len(masaq_words) and all(
        similarity(a, b.text) >= _MIN_SIMILARITY for a, b in zip(app_words, masaq_words)
    )


@dataclass
class MasaqAlignment:
    words: dict  # (ayah_id, word_index) -> [MasaqWord]
    unmatched_app_words: list
    merged: int
    # Ayahs whose MASAQ words pair up with the app's word for word. Only these get glosses:
    # where MASAQ splits or joins words differently, its glosses sometimes shift by a word.
    regular_ayahs: set


def align(masaq_ayahs: dict, app_words: dict) -> MasaqAlignment:
    words = {}
    unmatched = []
    merged = 0
    regular = set()
    for ayah_id in sorted(app_words):
        if pairs_word_for_word(app_words[ayah_id], masaq_ayahs.get(ayah_id, [])):
            regular.add(ayah_id)
        matches = align_ayah(app_words[ayah_id], masaq_ayahs.get(ayah_id, []))
        for index, masaq_words in enumerate(matches):
            if masaq_words:
                words[(ayah_id, index)] = masaq_words
                merged += len(masaq_words) > 1
            else:
                unmatched.append((ayah_id, index))
    return MasaqAlignment(words, unmatched, merged, regular)
