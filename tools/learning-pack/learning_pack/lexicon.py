"""The pack's vocabulary: roots and lemmas (dictionary words), with how often each occurs.

A lemma is what a learner memorises (decision 5): عَبَدَ "to worship" covers نَعۡبُدُ,
يَعۡبُدُونَ and every other form. The corpus names each stem's lemma in its LEM feature.
Every verb with the same root and verb form has the same LEM, so grouping is reliable.

What the corpus does not always give is a good *headword*, the form a dictionary shows.
For about 900 of its 1,475 verbs the LEM is an inflected form such as يَخۡدَعُ "he deceives"
rather than خَدَعَ "he deceived", the past tense that Arabic dictionaries list. So each
verb's headword is chosen in this order, and `headword_source` records which step gave it:

1. `corpus`: the corpus lemma already looks like a past-tense "he did" form.
2. `quran`: a past-tense "he did" form of this verb that occurs in the Quran.
3. `pattern`: built from the root and the verb form's pattern, for regular roots
   (for form I, the middle vowel comes from another past-tense form in the Quran).
4. `unchecked`: none of the above; the corpus lemma is kept for a person to check.
"""

import re
from collections import Counter, defaultdict
from dataclasses import dataclass
from typing import Optional

from .buckwalter import to_app

# Past tense "he did" by verb form, for a root C1 C2 C3 ({1} {2} {3}). "{{" is the
# hamzat al-wasl ٱ. Regular roots use _SOUND; roots with waw or yaa in some places, or the
# same last two letters, change shape in the forms listed in their own tables.
_SOUND = {
    "II": "{1}a{2}~a{3}a",
    "III": "{1}aA{2}a{3}a",
    "IV": ">a{1}o{2}a{3}a",
    "V": "ta{1}a{2}~a{3}a",
    "VI": "ta{1}aA{2}a{3}a",
    "VII": "{{n{1}a{2}a{3}a",
    "VIII": "{{{1}ota{2}a{3}a",
    "X": "{{sota{1}o{2}a{3}a",
}
# Middle letter waw or yaa (قَالَ, أَقَامَ, ٱسۡتَعَانَ). Forms II, III, V and VI keep it as a consonant.
_HOLLOW = {
    "I": "{1}aA{3}a",
    "IV": ">a{1}aA{3}a",
    "VII": "{{n{1}aA{3}a",
    "VIII": "{{{1}otaA{3}a",
    "X": "{{sota{1}aA{3}a",
}
# Last letter waw or yaa (سَوَّىٰ, أَعۡطَىٰ, ٱشۡتَرَىٰ). Form I is decided from the Quran.
_DEFECTIVE = {
    "II": "{1}a{2}~aY`",
    "III": "{1}aA{2}aY`",
    "IV": ">a{1}o{2}aY`",
    "V": "ta{1}a{2}~aY`",
    "VI": "ta{1}aA{2}aY`",
    "VII": "{{n{1}a{2}aY`",
    "VIII": "{{{1}ota{2}aY`",
    "X": "{{sota{1}o{2}aY`",
}
# Last two letters the same (مَدَّ, أَحَبَّ, ٱسۡتَقَرَّ).
_DOUBLED = {
    "I": "{1}a{2}~a",
    "II": "{1}a{2}~a{3}a",
    "III": "{1}aA{2}~a",
    "IV": ">a{1}a{2}~a",
    "V": "ta{1}a{2}~a{3}a",
    "VI": "ta{1}aA{2}~a",
    "VII": "{{n{1}a{2}~a",
    "VIII": "{{{1}ota{2}~a",
    "X": "{{sota{1}a{2}~a",
}

_HAMZA = set("A'><&}")
_WAW_YAA = set("wy")
# Form VIII changes its t after these first letters (ٱصۡطَبَرَ, ٱدَّكَرَ, ٱتَّقَىٰ), so it isn't built here.
_VIII_ASSIMILATING = set("SDTZdz*twy")
# Recitation marks that the corpus sometimes leaves at the end of a lemma.
_TRAILING_MARKS = "^@["
_HAMZA_SPELLINGS = set("A'><{|")


@dataclass
class Root:
    root_id: int
    key: str
    letters: str
    occurrences: int


@dataclass
class Lemma:
    lemma_id: int
    key: str
    headword: str
    headword_source: str
    pos: str
    root_key: Optional[str]
    verb_form: Optional[str]
    occurrences: int


def verb_form(segment) -> Optional[str]:
    """"I" to "XII" for verbs (the corpus marks forms II and up as "(II)" and so on)."""
    if segment.values.get("POS") != "V":
        return None
    for flag in segment.flags:
        if flag.startswith("(") and flag.endswith(")"):
            return flag[1:-1]
    return "I"


def root_letters(key: str) -> str:
    """Arabic root letters with spaces, e.g. "Ebd" -> "ع ب د". A hamza radical shows as ء."""
    return " ".join("ء" if letter in _HAMZA_SPELLINGS else to_app(letter) for letter in key)


# Detached pronouns by person, number and gender: the headword for words that are only a
# pronoun, which the corpus gives no lemma (هُوَ, and بِهِۦ = bi + hi).
_PRONOUNS = {
    "1S": ">anaA", "1P": "naHonu",
    "2MS": ">anta", "2FS": ">anti", "2D": ">antumaA", "2MP": ">antumo", "2FP": ">antun~a",
    "3MS": "huwa", "3FS": "hiYa", "3D": "humaA", "3MP": "humo", "3FP": "hun~a",
}
_PRONOUN_KEY = "PRON:"

# Nouns whose corpus lemma ends in a short -at written with ت where a dictionary writes ة
# (عِبَادَت -> عِبَادَة). عَنَت "hardship" really ends in ت.
_TAA_MARBUTA = re.compile(r"(?<=[^A`])at(?=\d*$)")
_REAL_FINAL_TAA = {"Eanat"}


def lemma_key(segment) -> Optional[str]:
    """The segment's lemma: the corpus's, or for a bare pronoun "PRON:" + its person (PRON:3MS)."""
    if segment.lemma:
        return segment.lemma
    if segment.tag == "PRON":
        person = next((flag for flag in segment.flags if flag in _PRONOUNS), None)
        return _PRONOUN_KEY + person if person else None
    return None


def display(buckwalter: str) -> str:
    """A lemma in Arabic, without the digit that tells homographs apart or trailing marks.

    A doubled first letter (نَّاس, from ٱلنَّاس) loses its shadda: no dictionary word starts
    with one.
    """
    text = re.sub(r"\d+$", "", buckwalter).rstrip(_TRAILING_MARKS)
    if len(text) > 1 and text[1] == "~":
        text = text[0] + text[2:]
    return to_app(text)


def headword_of(key: str, pos: str) -> str:
    """The Buckwalter text to display for a lemma that isn't a verb."""
    if key.startswith(_PRONOUN_KEY):
        return _PRONOUNS[key[len(_PRONOUN_KEY):]]
    if pos in ("N", "ADJ", "PN") and key not in _REAL_FINAL_TAA:
        return _TAA_MARBUTA.sub("ap", key)
    return key


def _starts_with_radical(text: str, radical: str) -> bool:
    if radical == "A":
        return text[:1] in _HAMZA_SPELLINGS
    return text.startswith(radical)


def _looks_like_past(lemma: str, root: Optional[str], form: str) -> bool:
    """True if the lemma looks like the past tense "he did" of its verb form."""
    text = re.sub(r"\d+$", "", lemma).rstrip(_TRAILING_MARKS)
    if not text.endswith(("a", "A", "Y", "`")):
        return False  # "he did" ends in a vowel; present and command forms don't
    if form in ("V", "VI"):
        return text.startswith("ta")
    if form in ("VII", "VIII", "IX", "X", "XI", "XII"):
        return text.startswith("{")
    if form == "IV":
        return text[:1] in _HAMZA_SPELLINGS
    return root is not None and _starts_with_radical(text, root[0])


def _form_one_vowel(c1: str, c2: str, c3: str, past_forms: Counter) -> Optional[str]:
    """The middle vowel of form I (فَعَلَ, فَعِلَ, فَعُلَ), read from past-tense forms in the Quran."""
    vowels = Counter()
    pattern = re.compile(f"^{re.escape(c1)}a{re.escape(c2)}([aiu]){re.escape(c3)}")
    for text, count in past_forms.items():
        match = pattern.match(text)
        if match:
            vowels[match.group(1)] += count
    return vowels.most_common(1)[0][0] if vowels else None


def _from_pattern(root: str, form: str, past_forms: Counter) -> Optional[str]:
    """Builds "he did" from the root and verb form, or None when the root is too irregular."""
    if len(root) != 3 or set(root) & _HAMZA:
        return None  # four-letter roots, and hamza, whose spelling depends on its vowels
    c1, c2, c3 = root
    if form == "VIII" and c1 in _VIII_ASSIMILATING:
        return None

    if c2 == c3 and c3 not in _WAW_YAA:
        table = _DOUBLED
    elif c3 in _WAW_YAA:
        # Checked before the middle letter: in roots like ح ي ي the last letter decides.
        if form == "I":
            vowel = _form_one_vowel(c1, c2, c3, past_forms)
            if vowel == "i":
                return f"{c1}a{c2}iya"  # رَضِيَ, نَسِيَ
            if vowel is None and not past_forms:
                return None
            return f"{c1}a{c2}aA" if c3 == "w" else f"{c1}a{c2}aY`"  # دَعَا, رَمَىٰ
        table = _DEFECTIVE
    elif c2 in _WAW_YAA and form in _HOLLOW:
        table = _HOLLOW
    elif form == "I":
        vowel = _form_one_vowel(c1, c2, c3, past_forms)
        return f"{c1}a{c2}{vowel}{c3}a" if vowel else None
    else:
        table = _SOUND

    template = table.get(form)
    # format() turns "{{" back into "{", the hamzat al-wasl.
    return template.format("", c1, c2, c3) if template else None


def build(words: list) -> tuple:
    """Returns (roots, lemmas) from the corpus words.

    Ids are given by frequency, most frequent first; ties by key. The ids are only used
    inside one pack; anything stored on a learner's device uses the lemma key instead.
    """
    occurrences = Counter()
    pos_counts = defaultdict(Counter)
    lemma_root = {}
    lemma_form = {}
    root_occurrences = Counter()
    past_3ms = defaultdict(Counter)  # lemma -> "he did" forms seen in the Quran
    past_any = defaultdict(Counter)  # lemma -> any active past-tense forms

    for word in words:
        for segment in word.stems:
            lemma = lemma_key(segment)
            if not lemma:
                continue  # detached pronouns and the disjointed letters have no lemma
            occurrences[lemma] += 1
            pos_counts[lemma][segment.values.get("POS", segment.tag)] += 1
            if segment.root:
                lemma_root[lemma] = segment.root
                root_occurrences[segment.root] += 1
            form = verb_form(segment)
            if form:
                lemma_form[lemma] = form
                if "PERF" in segment.flags and "PASS" not in segment.flags:
                    stem = segment.form.rstrip(_TRAILING_MARKS)
                    past_any[lemma][stem] += 1
                    if "3MS" in segment.flags:
                        past_3ms[lemma][stem] += 1

    lemmas = []
    for rank, key in enumerate(sorted(occurrences, key=lambda k: (-occurrences[k], k)), start=1):
        root = lemma_root.get(key)
        form = lemma_form.get(key)
        headword, source = headword_of(key, pos_counts[key].most_common(1)[0][0]), "corpus"
        if key.startswith(_PRONOUN_KEY):
            source = "pronoun"
        # A corpus lemma that is itself a "he did" form in the Quran is trusted even if it
        # doesn't fit its tagged verb form (تَعَٰلَىٰ is tagged form I).
        trusted = key.rstrip(_TRAILING_MARKS) in past_3ms[key]
        if form and not trusted and not _looks_like_past(key, root, form):
            found = [text for text, _ in past_3ms[key].most_common() if _looks_like_past(text, root, form)]
            if found:
                headword, source = found[0], "quran"
            else:
                built = _from_pattern(root, form, past_any[key]) if root else None
                headword, source = (built, "pattern") if built else (key, "unchecked")
        lemmas.append(Lemma(
            lemma_id=rank,
            key=key,
            headword=display(headword),
            headword_source=source,
            pos=pos_counts[key].most_common(1)[0][0],
            root_key=root,
            verb_form=form,
            occurrences=occurrences[key],
        ))

    roots = [
        Root(root_id=rank, key=key, letters=root_letters(key), occurrences=root_occurrences[key])
        for rank, key in enumerate(sorted(root_occurrences, key=lambda k: (-root_occurrences[k], k)), start=1)
    ]
    return roots, lemmas
