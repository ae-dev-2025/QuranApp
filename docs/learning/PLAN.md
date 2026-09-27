# Learning mode: implementation plan

> Status: approved 2026-09-27 · all recommended options chosen · follows [DESIGN.md](DESIGN.md)

Milestones 1–2 (PRs #1–#17) built the Read and Recite layers for 34 concepts. This plan
covers everything else a learner needs: words, grammar, a beginner path, practice and
review, audio, backup and other scripts. Every PR stays a 15–30 minute review and says
what it teaches.

---

## 1. Decisions

| # | Decision | Chosen |
|---|---|---|
| 1 | Where learning lives | a Learn tab in the bottom bar |
| 2 | How the path is organised | units anchored on short surahs, all four layers |
| 3 | The Understand sheet | a tab per layer over the ayah, word by word |
| 4 | The word card | extend today's word-by-word popup |
| 5 | What a vocabulary card is | a lemma (dictionary word), grouped by root |
| 6 | Grammar terms | English + the Arabic term |
| 7 | What counts as known | said-known and checked both count, shown differently |
| 8 | How practice is graded | automatically |
| 9 | How readiness is shown | a bar per layer, plus words % |
| 10 | Motivation | a calm weekly summary; reminders opt-in |
| 11 | Placement | choose a start, optional short check |
| 12 | Transliteration | stages 0–1 only, then on tap |
| 13 | How the data ships | an optional learning pack download |
| 14 | Sentence data | MASAQ (version 5, see below) |
| 15 | Review scheduler | FSRS |
| 16 | Build order | words first |
| 17 | Other scripts | analyse the Uthmani text, show results on any script by word position |

---

## 2. Data sources (pinned)

The raw files are not committed. `tools/learning-pack/README.md` explains how to get them,
and the pipeline refuses any file whose SHA-256 differs from the one below.

| Source | File | SHA-256 | Licence | Gives us |
|---|---|---|---|---|
| [Quranic Arabic Corpus](https://corpus.quran.com/download/) 0.4 | `quranic-corpus-morphology-0.4.txt` | `a1d12923815341face765083805d2148ed2d9f5cc3f7d6665219d887675d8c46` | GNU GPL; copy verbatim, credit and link to corpus.quran.com | lemma, root, part of speech and features of every word segment |
| [MASAQ](https://doi.org/10.17632/9yvrzxktmr.5) version 5 | `MASAQ.tsv` | `aac224f1b852a1a87e5a896b76c4b55df7c29369a7da836aea1b7286a9c3a931` | CC BY 4.0 | sentence roles (iʿrāb), case and mood |

MASAQ version 6 relabelled the same files CC BY-NC 3.0, which is not open source, so we use
version 5 only. Our own work (the pipeline, mappings, glosses, lessons) is GPL-3.0 like the
app. Every source is credited inside the app.

All scripts in `quranapp.db` split each of the 6,236 ayahs into the same words, so analysis
runs on the Uthmani text and its results are shown on any script by word position.

---

## 3. Open source, safety and security

- Only open data (GPL, CC BY 4.0) and our own GPL work. No NonCommercial or unlicensed data.
- One new network request: downloading the learning pack over HTTPS from a release on the
  fork. The app checks its SHA-256 before opening it and never runs code from it.
- No new permissions, analytics or tracking. Progress stays on the device and in the
  learner's own Export/Import file, which is validated on import.
- The pipeline is reproducible: the same inputs give the same pack.
- Lessons and analysis are flagged for a qualified teacher's review.
- Arabic review reference: [ILMHUB.org](https://www.ilmhub.org) (Ustadh Muhammad Arjan Ali). Grammar terms,
  definitions and examples (M8, M9) are checked against its Grammar Guide, written in our own words. It has no
  tajweed pages, so letter, makhārij, ṣifāt and tajweed texts still need a qualified teacher.

---

## 4. Milestones and PRs

PR numbers are planned; the list is updated as PRs open.

**M3 · Learning pack**

| PR | What it adds |
|---|---|
| 18 | This plan |
| 19 | Pipeline: read the corpus file; Buckwalter to Arabic |
| 20 | Pipeline: align corpus words to the app's words |
| 21 | Pipeline: lemma and root tables; write `learning_pack.db` with credits |
| 22 | Pipeline: MASAQ sentence roles, aligned to the app's words |
| 23 | App: open the pack (read-only Room database) |
| 24 | App: download the pack, check its hash |
| 25 | App: Learning data screen with download, delete and credits |

**M4 · Words**

| PR | What it adds |
|---|---|
| 26 | Word analysis for an ayah from the pack |
| 27 | Understand sheet: a tab per layer |
| 28 | Words tab: the ayah word by word, word rows, words known % |
| 29 | Word popup: "Learn this word" |
| 30 | Root page |
| 31 | Word examples in the Quran |
| 32 | Pipeline: English meanings for lemmas, from MASAQ's word glosses |
| 33 | Pipeline: checked meanings for the most frequent words |
| 34 | Pipeline: cleaner headwords; lemmas for bare pronouns |
| 35 | Pack v2 in the app, with meanings on screen |

Pack v3 (with the next pipeline change): checked meanings for إِيَّا ("him alone, me alone" reads
oddly), وَلَدَ ("born, gave" → "to beget"), صَمَد ("eternal the absolute" → "the Eternal Refuge"),
and overrides for the 258 verb headwords still marked "unchecked".

**M5 · Practice and review**

| PR | What it adds |
|---|---|
| 36 | Review tables in `user_db` (version 4) |
| 37 | FSRS scheduler |
| 38 | Question model and word questions |
| 39 | Reading and tajweed "tap the word" questions |
| 40 | Practice session and question factory |
| 41 | Practice screen and "Check yourself" |
| 42 | Learn tab with the daily review |
| 43 | Learning progress in Export/Import |

**M6 · Learn tab and path**

| PR | What it adds |
|---|---|
| 44 | Curriculum: stages, surah units and readiness |
| 45 | Unit screen |
| 46 | Learn screen: where you left off and your path (readiness bars shipped in #45) |
| 47 | The whole path: every stage and unit |
| 48 | Placement |
| 49 | Goals |
| 50 | Progress and weekly summary |
| 51 | Today's new words: introduced, then checked (the design's daily load) |
| 52 | Learning settings: new words a day, where you start, reset; settings in Export/Import |
| 53 | Opt-in reminder when reviews are due |

**M7 · Letters and sounds**

| PR | What it adds |
|---|---|
| 54 | Letters track |
| 55 | Letter grid and letter pages |
| 56 | Audio for words and examples ("Hear it") |
| 57 | Where letters are made (makhārij) |
| 58 | Concept stages: a stage's goals count only the concepts it teaches |
| 59 | Letter qualities (ṣifāt) |
| 60 | Letter questions |

**M8 · Grammar I: word forms**

| PR | What it adds |
|---|---|
| 61 | Grammar track: 70 concepts and their prerequisites |
| 62 | Grammar detector: word features to concepts |
| 63 | Grammar tab: the fourth layer (sheet tab, unit step, path dot, stage goals); Arabic examples kept in order in English text |
| 64 | Grammar lessons, units 1–2 (word types; gender, number, definiteness); detector fixes for Uthmani spelling |
| 65 | Grammar examples in the Quran, from the pack |
| 66 | Lessons: cases, pronouns and relatives, prepositions, adjectives; proper nouns have cases |
| 67 | Lessons: roots and patterns, past, present and moods, command and passive, inna and kāna |
| 68 | Lessons: verb forms and weak verbs, participles and verbal nouns |
| 69 | Lessons: conditions, questions and particles |

**M9 · Grammar II: sentences**

| PR | What it adds |
|---|---|
| 70 | Sentence roles to concepts (MASAQ) |
| 71 | Each word's role in the Grammar tab (iʿrāb) |
| 72 | Lessons: iḍāfa, the nominal and the verbal sentence |
| 73 | Lessons: the other objects, ḥāl, tamyīz, and word shapes |
| 74 | Grammar questions |

**M10 · Complete recitation**

Each PR ships its concepts' lessons too, so every concept keeps a lesson.

| PR | What it adds |
|---|---|
| 75 | Heavy and light rāʾ (an umbrella and two rules) |
| 76 | Idghām of two letters, lām of verbs and particles, tanwīn before hamzat al-waṣl |
| 77 | Stopping on a word; madds ʿāriḍ, līn, ʿiwaḍ, badal and ṣila |
| 78 | Disjointed letters and their madd, the four saktas, Ḥafṣ's special words, starting on hamzat al-waṣl, special spellings |

**M11 · Polish**

| PR | What it adds |
|---|---|
| 79 | Show learning screens in the reader's script (Indo-Pak) |
| 80 | Dark theme and right-to-left fixes |
| 81 | Accessibility |
| 82 | Speed: caching |
| 83 | Security and licence review, including: Export/Import crashes on a file that isn't JSON (found in #43; the parse in `ActivityExportImport.importData` isn't caught) |
