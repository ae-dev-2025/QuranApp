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
| 46 | Learn screen: where you left off and your path |
| 47 | Readiness bars |
| 48 | Placement |
| 49 | Goals |
| 50 | Progress and weekly summary |
| 51 | Learning settings |

**M7 · Letters and sounds**

| PR | What it adds |
|---|---|
| 52 | Letters track |
| 53 | Letter grid and letter pages |
| 54 | Audio for words and examples ("Hear it") |
| 55–56 | Where letters are made (makhārij) and letter qualities (ṣifāt) |
| 57 | Letter questions |

**M8 · Grammar I: word forms**

| PR | What it adds |
|---|---|
| 58 | Grammar track: 70 concepts and their prerequisites |
| 59 | Grammar detector: word features to concepts |
| 60 | Grammar tab |
| 61–65 | Lessons for word-form concepts |

**M9 · Grammar II: sentences**

| PR | What it adds |
|---|---|
| 66 | Sentence roles to concepts |
| 67 | Sentence roles in the Grammar tab |
| 68–71 | Lessons for sentence concepts |
| 72 | Grammar questions |

**M10 · Complete recitation**

| PR | What it adds |
|---|---|
| 73 | Heavy and light rāʾ |
| 74 | Idghām of two letters, lām of verbs and particles, tanwīn before hamzat al-waṣl |
| 75 | Madds: ʿāriḍ, līn, ʿiwaḍ, badal, ṣila, disjointed letters |
| 76 | Saktas, Ḥafṣ's special words, stopping and starting, special spellings |
| 77–79 | Lessons for the new concepts |

**M11 · Polish**

| PR | What it adds |
|---|---|
| 80 | Show learning screens in the reader's script (Indo-Pak) |
| 81 | Dark theme and right-to-left fixes |
| 82 | Accessibility |
| 83 | Speed: caching |
| 84 | Security and licence review, including: Export/Import crashes on a file that isn't JSON (found in #43; the parse in `ActivityExportImport.importData` isn't caught) |
