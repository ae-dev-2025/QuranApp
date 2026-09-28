# Learning mode: security and licence review

Reviewed for #84, over everything learning mode adds to the app (`learning/`, the Export and
Import changes, the manifest, `tools/learning-pack/`). Each area says what was checked, what was
found, and what was done. Open points need a person's decision.

## Network

- **One connection:** downloading the learning pack. Nothing else is sent or fetched. There is
  no analytics and no account, and nothing about the learner leaves the phone.
- **Only the reviewed file is installed.** The URL, sizes and SHA-256 hashes of both the
  download and the unzipped pack are constants in `LearningPackRelease`. A new pack means a
  reviewed PR.
- **Checked before it's used.** `PackInstaller` hashes every byte and stops as soon as a
  file is bigger than expected, so a bad server can't fill the phone's storage. It unzips next
  to the target and renames it into place, so the installed pack is never half written.
- **HTTPS only:** `MODERN_TLS`, and no redirect from HTTPS to plain HTTP. The app's shared
  client still allows HTTP for other servers; that isn't changed.
- **Word audio** uses the app's existing word-by-word audio source. It isn't new.

## Data on the phone

- **Progress** lives in the app's private database: item ids (`word.…`, `reading.…`), grades
  and times. Nothing personal is stored.
- **The pack and the grammar index** made from it live in the app's private `files/learning/`.
- **Backups.** *Found:* the app backs up everything to Google Drive, but Android keeps only
  25 MB per app. The pack alone is 25.5 MB, so once it's downloaded, the whole backup could
  fail, including bookmarks and learning progress. *Fixed:* `files/learning/` is left out of
  backups (`data_extraction_rules.xml` on Android 12+, `backup_rules.xml` before). It can be
  downloaded again.

## Import

- **Found (#43):** picking a file that isn't JSON, or can't be read, crashed the app. The
  parse in `ActivityExportImport.importData` ran uncaught in a coroutine. A huge file was also
  read whole into memory. *Fixed:* the app says "This file isn't a QuranApp backup, so nothing
  was imported", and stops reading after 32 million characters. The error isn't logged,
  because a parse error quotes the file, which could be anything the user picked.
- **The learning section** was already checked entry by entry (`LearningBackups.check`):
  - the format version
  - valid item ids
  - times after 2020 and not in the future (allowing a day for another phone's clock)
  - at most 50,000 known items, 50,000 cards and 500,000 log entries

  An import only adds to this phone's progress and never removes anything.

## App components

- **Nine learning screens**, none exported. They have no intent filters, so other apps
  can't open them (confirmed: `am start` from outside is refused).
- **The reminder** is opt-in. It uses WorkManager, and its `PendingIntent` is `FLAG_IMMUTABLE`.
  It asks for the notification permission only when it's turned on.
- **No new permissions** and **no new dependencies**.

## Code

- **SQL:** Room queries with bound parameters only. No raw SQL anywhere in `learning/`.
- **Logging:** learning mode writes no logs.
- **Local files it reads** (the shipped concept index, the saved grammar index) are parsed
  inside `runCatching`. A damaged file is ignored or made again, never trusted.

## Licences

| What | Licence | How it's met |
|---|---|---|
| The app, including learning mode | GPL-3.0 (the upstream licence) | All code is in this repository |
| Quranic Arabic Corpus 0.4 (morphology) | GNU GPL, plus the corpus's own terms (below) | Credited with its full notice and a tappable link in Settings → Learning → Learning data |
| MASAQ **version 5** (syntax, word glosses) | CC BY 4.0 | Credited as above; the pipeline pins v5 by SHA-256, because v6 was relabelled CC BY-NC 3.0, which isn't open source |
| English word meanings | derived from MASAQ glosses (CC BY 4.0), plus reviewed overrides | `tools/learning-pack/data/meaning_overrides.tsv` |
| `concept_index.txt` | made from the app's own Quran text | Only ayah numbers, no text |
| Fonts (Uthmanic Hafs, Scheherazade New) | the upstream app's existing fonts | Nothing new is bundled |
| ILMHUB | used as a reference when reviewing Arabic | No text copied; lessons are written for this app |

**The Quranic Arabic Corpus's terms** (checked on corpus.quran.com/download, 2026-09-28).
The corpus file says copies of *the file* must be verbatim. Separately, the terms let the
annotation be used "in any website or application", as long as:
- the source is clearly named
- a link is made to corpus.quran.com
- the copyright notice is reproduced in works derived from it

The pack doesn't ship the corpus file. The app names the corpus, shows its full notice, and
(since this review) links to corpus.quran.com. So the terms are met.

## Open points for a person

1. **A teacher's review** of the reading and tajweed lessons. ILMHUB covers grammar, but it
   has no tajweed pages.
2. **258 verb headwords** are marked `unchecked` in the pack (4% of verb occurrences).
3. **Import size vs. learning limits.** The learning format allows up to 500,000 log
   entries, about 46 million characters. That's more than the 32 million an import reads.
   Reaching it would take around 400,000 reviews, so it isn't a problem in practice. If it
   ever matters, the log limit can come down.
