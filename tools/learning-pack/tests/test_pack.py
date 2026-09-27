import sqlite3
import tempfile
import unittest
from pathlib import Path

from learning_pack import lexicon, pack
from learning_pack.align import AlignedWord
from learning_pack.buckwalter import to_app
from learning_pack.corpus import group_words, parse_segments
from learning_pack.sources import CORPUS

# Al-Fatihah 1:5 from the Quranic Arabic Corpus 0.4 (GNU GPL, corpus.quran.com).
LINES = [
    "(1:5:1:1)\t<iy~aAka\tPRON\tSTEM|POS:PRON|LEM:<iy~aA|2MS\r\n",
    "(1:5:2:1)\tnaEobudu\tV\tSTEM|POS:V|IMPF|LEM:Eabada|ROOT:Ebd|1P\r\n",
    "(1:5:3:1)\twa\tCONJ\tPREFIX|w:CONJ+\r\n",
    "(1:5:3:2)\t<iy~aAka\tPRON\tSTEM|POS:PRON|LEM:<iy~aA|2MS\r\n",
    "(1:5:4:1)\tnasotaEiynu\tV\tSTEM|POS:V|IMPF|(X)|LEM:{sotaEiynu|ROOT:Ewn|1P\r\n",
]


class PackTest(unittest.TestCase):
    def setUp(self):
        self.folder = tempfile.TemporaryDirectory()
        self.path = Path(self.folder.name) / "learning_pack.db"
        words = group_words(parse_segments(LINES))
        aligned = [AlignedWord(1005, index, "", word) for index, word in enumerate(words)]
        roots, lemmas = lexicon.build(words)
        pack.write(self.path, aligned, roots, lemmas, [CORPUS], {"pack_version": "1"}, to_app)
        self.db = sqlite3.connect(self.path)

    def tearDown(self):
        self.db.close()
        self.folder.cleanup()

    def test_room_version_is_set(self):
        self.assertEqual(self.db.execute("PRAGMA user_version").fetchone()[0], pack.SCHEMA_VERSION)

    def test_segments_point_at_their_lemma(self):
        rows = self.db.execute(
            "SELECT s.word_index, s.segment_index, s.kind, s.features, l.lemma_key "
            "FROM segments s LEFT JOIN lemmas l ON l.lemma_id = s.lemma_id ORDER BY 1, 2"
        ).fetchall()
        self.assertEqual(rows, [
            (0, 0, "STEM", "2MS", "<iy~aA"),
            (1, 0, "STEM", "IMPF|1P", "Eabada"),
            (2, 0, "PREFIX", "w:CONJ+", None),
            (2, 1, "STEM", "2MS", "<iy~aA"),
            (3, 0, "STEM", "IMPF|(X)|1P", "{sotaEiynu"),
        ])

    def test_lemmas_have_roots_and_counts(self):
        rows = self.db.execute(
            "SELECT l.lemma_key, l.occurrences, r.letters FROM lemmas l "
            "LEFT JOIN roots r ON r.root_id = l.root_id ORDER BY l.lemma_id"
        ).fetchall()
        self.assertEqual(rows, [("<iy~aA", 2, None), ("Eabada", 1, "\u0639 \u0628 \u062f"), ("{sotaEiynu", 1, "\u0639 \u0648 \u0646")])

    def test_the_source_is_credited(self):
        name, licence, url = self.db.execute("SELECT name, licence, url FROM credits").fetchone()
        self.assertEqual(name, CORPUS.name)
        self.assertEqual(url, "https://corpus.quran.com")

    def test_an_existing_file_is_never_overwritten(self):
        with self.assertRaises(FileExistsError):
            pack.write(self.path, [], [], [], [], {}, to_app)


if __name__ == "__main__":
    unittest.main()
