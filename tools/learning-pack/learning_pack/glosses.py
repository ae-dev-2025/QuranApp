"""English meanings for lemmas (dictionary words), derived from MASAQ's word glosses.

MASAQ glosses each word in its ayah: نَعۡبُدُ is "we worship", رَبِّكَ is "your lord", and
words added for English sense are in brackets ("(the) day"). A lemma's meaning is what its
words' glosses have in common once the parts that come from grammar are removed: the
subject of a verb ("we"), "and"/"so" from a prefix, "the" or "your" before a noun. The most
common remaining gloss wins, with a second meaning when that one is common too:
مِن -> "from, of".

Only words with a single dictionary word are used (وَمِمَّا "and from what" mixes two), and
only in the ayahs whose glosses MASAQ lines up with its words (see masaq.py).

The result is a starting point: overrides.tsv replaces it for words a person has checked.
"""

import re
from collections import Counter
from pathlib import Path

from .lexicon import lemma_key

OVERRIDES = Path(__file__).resolve().parent.parent / "data" / "meaning_overrides.tsv"

_BRACKETS = re.compile(r"\([^)]*\)|\[[^\]]*\]")

# Words that come from prefixes or the sentence, not from the dictionary word itself.
_LEADING_ALWAYS = {"and", "so", "then", "but", "or", "o", "nor"}
_LEADING_CONTENT = {
    "indeed", "surely", "verily", "certainly", "truly", "is", "are", "was", "were", "be", "been", "being",
    "will", "shall", "would", "should", "could", "can", "may", "might", "did", "do", "does", "has",
    "have", "had", "let", "that", "to", "who", "whom", "which",
}
_SUBJECTS = {"i", "you", "he", "she", "it", "we", "they", "you all", "those", "those who", "one", "the one"}
_OBJECTS = {"him", "her", "them", "it", "you", "me", "us", "to him", "to her", "to them", "to it", "to you",
            "to me", "to us", "for them", "for you", "for him", "for us"}
_PRONOUN_ENDINGS = {"him", "her", "them", "it", "you", "me", "us"}
_DETERMINERS = {"the", "a", "an", "my", "your", "his", "her", "its", "our", "their", "this", "that",
                "these", "those", "of", "in", "to", "for", "with", "by", "from", "on", "at", "all"}
_CONTENT_TAGS = {"V", "N", "ADJ", "PN"}
# A second meaning is shown when it is at least this common, relative to the first.
_SECOND_MEANING_SHARE = 0.3


def _strip_leading(words: list, stop: set) -> list:
    while len(words) > 1 and words[0] in stop:
        words = words[1:]
    return words


def _strip_phrase(text: str, phrases: set, at_start: bool) -> str:
    # Longest phrases first, so "you all" goes before "you". Never leave nothing behind.
    for phrase in sorted(phrases, key=len, reverse=True):
        if at_start and text.startswith(phrase + " "):
            return text[len(phrase) + 1:]
        if not at_start and text.endswith(" " + phrase):
            return text[: -len(phrase) - 1]
    return text


def normalise(gloss: str, pos: str) -> str:
    """The part of a word's gloss that belongs to its dictionary word."""
    text = _BRACKETS.sub(" ", gloss.lower())
    text = " ".join(text.replace("-", " ").split())
    words = _strip_leading(text.split(), _LEADING_ALWAYS)
    if pos in _CONTENT_TAGS:
        words = _strip_leading(words, _LEADING_CONTENT)
        text = " ".join(words)
        if pos == "V":
            text = _strip_phrase(text, _SUBJECTS, at_start=True)
            text = _strip_phrase(text, _OBJECTS, at_start=False)
            text = " ".join(_strip_leading(text.split(), _LEADING_CONTENT))
        else:
            text = " ".join(_strip_leading(text.split(), _DETERMINERS))
        words = text.split()
    # "between them", "before them": the pronoun is an attached suffix, not part of the word.
    return _strip_phrase(" ".join(words), _PRONOUN_ENDINGS, at_start=False)


def derive(glosses_by_lemma: dict, pos_by_lemma: dict) -> dict:
    """lemma key -> meaning, from lemma key -> Counter of its words' MASAQ glosses."""
    meanings = {}
    for key, glosses in glosses_by_lemma.items():
        pos = pos_by_lemma.get(key, "")
        counts = Counter()
        for gloss, count in glosses.items():
            meaning = normalise(gloss, pos)
            if meaning:
                counts[meaning] += count
        if not counts:
            continue
        ranked = sorted(counts.items(), key=lambda item: (-item[1], len(item[0]), item[0]))
        first, first_count = ranked[0]
        meaning = first
        if len(ranked) > 1:
            second, second_count = ranked[1]
            # Skip a second meaning that only adds to or repeats the first ("from" / "from them").
            overlaps = second in first or first in second
            if second_count >= first_count * _SECOND_MEANING_SHARE and not overlaps:
                meaning = f"{first}, {second}"
        if pos == "PN":
            meaning = meaning[:1].upper() + meaning[1:]
        meanings[key] = meaning
    return meanings


def collect(aligned: list, masaq_words: dict, regular_ayahs: set) -> dict:
    """lemma key -> Counter of the MASAQ glosses of words made of that lemma alone."""
    collected = {}
    for word in aligned:
        if word.ayah_id not in regular_ayahs:
            continue
        stems = [stem for stem in word.corpus_word.stems if lemma_key(stem)]
        masaq = masaq_words.get((word.ayah_id, word.word_index))
        if len(stems) != 1 or not masaq:
            continue
        gloss = " ".join(part.gloss for part in masaq if part.gloss and part.gloss != "#N/A")
        if gloss:
            collected.setdefault(lemma_key(stems[0]), Counter())[gloss] += 1
    return collected


def read_overrides(path: Path = OVERRIDES) -> dict:
    """lemma key -> checked meaning, from a tab-separated file with a header and # comments."""
    overrides = {}
    with open(path, encoding="utf-8") as file:
        rows = [line.rstrip("\n") for line in file if line.strip() and not line.startswith("#")]
    for row in rows[1:]:  # skip the header
        key, meaning = row.split("\t")
        if key in overrides:
            raise ValueError(f"{key} is overridden twice")
        overrides[key] = meaning
    return overrides


def apply_overrides(meanings: dict, overrides: dict, lemma_keys: set) -> dict:
    """The meanings with the checked ones in place. A key that isn't a lemma is an error, so a
    typo or a changed corpus can't silently drop a checked meaning."""
    unknown = sorted(set(overrides) - lemma_keys)
    if unknown:
        raise ValueError(f"overrides for lemmas that don't exist: {unknown}")
    return {**meanings, **overrides}
