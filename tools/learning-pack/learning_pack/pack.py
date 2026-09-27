"""Writes learning_pack.db.

The app opens this file with Room, which checks that every table matches its Kotlin entity
exactly (column names, types, NOT NULL, primary keys, indexes). So SCHEMA below is written
the way Room writes its own CREATE statements, and PRAGMA user_version is the Room
database version. When SCHEMA changes, the entities and that version change with it.

Rows are inserted in a fixed order and the file is vacuumed, so the same inputs always
give the same pack.
"""

import sqlite3
from pathlib import Path

SCHEMA_VERSION = 1

SCHEMA = [
    "CREATE TABLE IF NOT EXISTS `meta` (`key` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`key`))",
    "CREATE TABLE IF NOT EXISTS `credits` (`credit_id` INTEGER NOT NULL, `name` TEXT NOT NULL, "
    "`licence` TEXT NOT NULL, `url` TEXT NOT NULL, `notice` TEXT NOT NULL, PRIMARY KEY(`credit_id`))",
    "CREATE TABLE IF NOT EXISTS `roots` (`root_id` INTEGER NOT NULL, `root_key` TEXT NOT NULL, "
    "`letters` TEXT NOT NULL, `occurrences` INTEGER NOT NULL, PRIMARY KEY(`root_id`))",
    "CREATE UNIQUE INDEX IF NOT EXISTS `index_roots_root_key` ON `roots` (`root_key`)",
    "CREATE TABLE IF NOT EXISTS `lemmas` (`lemma_id` INTEGER NOT NULL, `lemma_key` TEXT NOT NULL, "
    "`headword` TEXT NOT NULL, `headword_source` TEXT NOT NULL, `pos` TEXT NOT NULL, `root_id` INTEGER, "
    "`verb_form` TEXT, `occurrences` INTEGER NOT NULL, `gloss` TEXT, PRIMARY KEY(`lemma_id`))",
    "CREATE UNIQUE INDEX IF NOT EXISTS `index_lemmas_lemma_key` ON `lemmas` (`lemma_key`)",
    "CREATE INDEX IF NOT EXISTS `index_lemmas_root_id` ON `lemmas` (`root_id`)",
    "CREATE TABLE IF NOT EXISTS `segments` (`ayah_id` INTEGER NOT NULL, `word_index` INTEGER NOT NULL, "
    "`segment_index` INTEGER NOT NULL, `form` TEXT NOT NULL, `kind` TEXT NOT NULL, `tag` TEXT NOT NULL, "
    "`lemma_id` INTEGER, `features` TEXT NOT NULL, PRIMARY KEY(`ayah_id`, `word_index`, `segment_index`))",
    "CREATE INDEX IF NOT EXISTS `index_segments_lemma_id` ON `segments` (`lemma_id`)",
]

_VALUE_KEYS = ("PRON", "SP", "MOOD")


def features(segment) -> str:
    """Everything about a segment except its lemma and root, which have their own columns.

    For example "IMPF|(X)|1P" for نَسۡتَعِينُ, or "PRON:2MS" for the -ka of إِيَّاكَ.
    """
    parts = list(segment.flags)
    parts += [f"{key}:{segment.values[key]}" for key in _VALUE_KEYS if key in segment.values]
    return "|".join(parts)


def write(path: Path, aligned: list, roots: list, lemmas: list, credits: list, meta: dict, to_arabic) -> None:
    """Writes a new pack at `path` (which must not exist yet)."""
    if path.exists():
        raise FileExistsError(path)
    root_ids = {root.key: root.root_id for root in roots}
    lemma_ids = {lemma.key: lemma.lemma_id for lemma in lemmas}

    connection = sqlite3.connect(path)
    try:
        for statement in SCHEMA:
            connection.execute(statement)
        connection.executemany(
            "INSERT INTO meta VALUES (?, ?)", sorted(meta.items())
        )
        connection.executemany(
            "INSERT INTO credits VALUES (?, ?, ?, ?, ?)",
            [(index, source.name, source.licence, source.url, source.notice)
             for index, source in enumerate(credits, start=1)],
        )
        connection.executemany(
            "INSERT INTO roots VALUES (?, ?, ?, ?)",
            [(root.root_id, root.key, root.letters, root.occurrences) for root in roots],
        )
        connection.executemany(
            "INSERT INTO lemmas VALUES (?, ?, ?, ?, ?, ?, ?, ?, NULL)",
            [(lemma.lemma_id, lemma.key, lemma.headword, lemma.headword_source, lemma.pos,
              root_ids.get(lemma.root_key), lemma.verb_form, lemma.occurrences) for lemma in lemmas],
        )
        connection.executemany(
            "INSERT INTO segments VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            [(word.ayah_id, word.word_index, segment.index - 1, to_arabic(segment.form), segment.kind,
              segment.tag, lemma_ids.get(segment.lemma) if segment.kind == "STEM" else None, features(segment))
             for word in aligned for segment in word.corpus_word.segments],
        )
        connection.execute(f"PRAGMA user_version = {SCHEMA_VERSION}")
        connection.commit()
        connection.execute("VACUUM")
    finally:
        connection.close()
