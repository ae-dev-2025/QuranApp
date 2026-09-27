import tempfile
import unittest
from pathlib import Path

from learning_pack.sources import CORPUS, SourceMismatch, sha256_of, verify


class SourcesTest(unittest.TestCase):
    def test_a_different_file_is_refused(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / CORPUS.file_name
            path.write_text("not the corpus", encoding="utf-8")
            with self.assertRaises(SourceMismatch):
                verify(path, CORPUS)

    def test_hash_of_known_bytes(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "abc.txt"
            path.write_bytes(b"abc")
            self.assertEqual(
                sha256_of(path),
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            )


if __name__ == "__main__":
    unittest.main()
