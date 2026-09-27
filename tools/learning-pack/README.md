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
