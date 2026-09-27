# Learning pack pipeline

Builds `learning_pack.db`, the optional download behind the learning mode's Words and
Grammar layers: the dictionary form (lemma), root and grammar of every word in the Quran,
lined up with the app's own words.

It is plain Python 3.10+ with no third-party packages, so it can be audited and rerun by
anyone.

## Inputs

The raw data is not stored in this repository. Put both files in one folder:

| File | Where to get it | Licence |
|---|---|---|
| `quranic-corpus-morphology-0.4.txt` | <https://corpus.quran.com/download/> (asks for a contact email), inside `quranic-corpus-morphology-0.4.zip` | GNU GPL; copy verbatim, credit and link to corpus.quran.com |
| `MASAQ.tsv` | <https://doi.org/10.17632/9yvrzxktmr.5>, **version 5** | CC BY 4.0 |

MASAQ version 6 relabelled the same files CC BY-NC 3.0, which is not open source. Use
version 5. The pipeline checks each file's SHA-256 (`learning_pack/sources.py`) and refuses
anything else.

## Tests

```bash
cd tools/learning-pack
python -m unittest discover -s tests -t .
```

`escape_arabic.py` rewrites Arabic in test files as `\uXXXX` escapes. Editors often reorder
"shadda + vowel", which would silently change what a test checks.

## Build

```bash
cd tools/learning-pack
python -m learning_pack.build --data ../../../data \
    --quran-db ../../app/src/main/assets/db/quranapp.db \
    --out build/learning_pack.db --version 1
```

The same inputs always give the same file (same SHA-256).

## What's in the pack

| Table | One row per | Notes |
|---|---|---|
| `segments` | prefix, stem or suffix of each of the app's 77,429 words | Arabic form, part of speech, features such as `IMPF\|(X)\|1P`, lemma of stems |
| `lemmas` | dictionary word (4,832) | headword, root, verb form, number of occurrences, gloss (added later) |
| `roots` | root (1,642) | letters such as `ع ب د`, number of occurrences |
| `credits`, `meta` | source, setting | licences and notices shown in the app; pack and schema versions |

Many corpus lemmas for verbs are inflected forms (يَخۡدَعُ). `lexicon.py` picks a
dictionary headword (خَدَعَ) and records where it came from in `headword_source`:
`corpus`, `quran`, `pattern` or `unchecked` (258 verbs, 4% of verb occurrences, left for a
person to check).
