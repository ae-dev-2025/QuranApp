"""Lines the corpus's words up with the app's words.

The app stores each ayah as numbered words in `quranapp.db` (table `ayah_words`, script
`uthmani`), counted from 0, with the ayah number as an extra last "word". The corpus counts
words from 1. So corpus word w is app word w - 1.

Position alone would silently pair the wrong words if either side split an ayah
differently, so every pair is also compared letter by letter. Both texts are Uthmani but
come from different editions, so the comparison ignores vowel marks and folds the few
letters the editions write differently. A pair that still differs stops the build, unless
a person has checked it and listed it in KNOWN_SPELLING_DIFFERENCES.
"""

import sqlite3
import unicodedata
from dataclasses import dataclass
from pathlib import Path

UTHMANI_SCRIPT_ID = 1
AYAH_COUNT = 6236

# (ayah_id, app word_index): places where the two editions spell a word differently.
# Checked by hand against both texts; the words are the same, only the spelling differs.
KNOWN_SPELLING_DIFFERENCES = {
    (2072, 3): "فَٱدَّٰرَٰءۡتُمۡ: the app writes the hamza on the line, the corpus above the alif",
    (12039, 0): "يَٰصَٰحِبَيِ: the corpus adds an alif maqsura before the dagger alif",
    (12041, 0): "يَٰصَٰحِبَيِ: same as 12:39",
}

_FOLD = {
    "ٱ": "ا",  # hamzat al-wasl -> alif
    "أ": "ا",  # alif with hamza above
    "إ": "ا",  # alif with hamza below
    "آ": "ا",  # alif with madda
    "ؤ": "و",  # waw with hamza
    "ئ": "ي",  # yaa with hamza
    "ى": "ي",  # alif maqsura
}


def skeleton(text: str) -> str:
    """The bare letters of a word: no vowels, marks, signs or spaces, a few letters folded.

    The corpus writes a hamza before a long alif (ءا) where the app writes the alif with a
    madda (آ), so that pair folds to a plain alif too.
    """
    letters = []
    for char in text:
        # Mn: vowels and Quranic marks. Lm: small waw/yaa. So: ۞ and ۩, which the app
        # attaches to words. Also tatweel and the space in names such as إِلۡ يَاسِينَ.
        if unicodedata.category(char) in ("Mn", "Lm", "So") or char in "ـ ":
            continue
        letters.append(_FOLD.get(char, char))
    return "".join(letters).replace("ءا", "ا")


def load_app_words(quran_db: Path) -> dict:
    """ayah_id -> the ayah's words in the Uthmani script, without the ayah number."""
    ayahs = {}
    with sqlite3.connect(f"file:{quran_db}?mode=ro", uri=True) as connection:
        rows = connection.execute(
            "SELECT ayah_id, text FROM ayah_words WHERE script_id = ? ORDER BY ayah_id, word_index",
            (UTHMANI_SCRIPT_ID,),
        )
        for ayah_id, text in rows:
            ayahs.setdefault(ayah_id, []).append(text)
    for ayah_id, words in ayahs.items():
        if not words[-1].isdigit():
            raise AlignmentError(f"ayah {ayah_id} does not end with its number")
        del words[-1]
    return ayahs


@dataclass(frozen=True)
class AlignedWord:
    ayah_id: int
    word_index: int
    app_text: str
    corpus_word: object  # corpus.Word


class AlignmentError(Exception):
    pass


def align(corpus_words: list, app_words: dict, to_arabic, expected_ayahs: int = AYAH_COUNT) -> list:
    """Pairs every corpus word with its app word, in order, or raises AlignmentError."""
    by_ayah = {}
    for word in corpus_words:
        by_ayah.setdefault(word.surah * 1000 + word.ayah, []).append(word)

    problems = []
    if len(app_words) != expected_ayahs or set(by_ayah) != set(app_words):
        problems.append(f"ayahs: app {len(app_words)}, corpus {len(by_ayah)}")

    aligned = []
    for ayah_id in sorted(app_words):
        app_ayah = app_words[ayah_id]
        corpus_ayah = by_ayah.get(ayah_id, [])
        if len(app_ayah) != len(corpus_ayah):
            problems.append(f"{ayah_id}: app {len(app_ayah)} words, corpus {len(corpus_ayah)}")
            continue
        for index, (app_text, corpus_word) in enumerate(zip(app_ayah, corpus_ayah)):
            if corpus_word.word != index + 1:
                problems.append(f"{ayah_id}: corpus word {corpus_word.word} at position {index}")
            same = skeleton(app_text) == skeleton(to_arabic(corpus_word.form))
            if not same and (ayah_id, index) not in KNOWN_SPELLING_DIFFERENCES:
                problems.append(f"{ayah_id}:{index}: app {app_text}, corpus {to_arabic(corpus_word.form)}")
            aligned.append(AlignedWord(ayah_id, index, app_text, corpus_word))

    if problems:
        shown = "\n".join(problems[:20])
        raise AlignmentError(f"{len(problems)} alignment problems:\n{shown}")
    return aligned
