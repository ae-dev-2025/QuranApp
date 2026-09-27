import sqlite3
import tempfile
import unittest
from pathlib import Path

from learning_pack import lexicon, masaq, pack
from learning_pack.buckwalter import to_app
from learning_pack.sources import MASAQ

HEADER = (
    "ID\tSura_No\tVerse_No\tWord_No\tSegment_No\tWord\tWithout_Diacritics\tSegmented_Word\tMorph_Tag\t"
    "Morph_Type\tPunctuation_Mark\tInvariable_Declinable\tSyntactic_Role\tPossessive_Construct\tCase_Mood\t"
    "Case_Mood_Marker\tPhrase\tPhrasal_Function\tGloss"
)

# Rows from MASAQ version 5 (CC BY 4.0, doi:10.17632/9yvrzxktmr.5): Al-Fatihah 1:5 and the
# start of Al-A'raf 7:61, where MASAQ writes يَٰقَوۡمِ as two words and its glosses shift.
ROWS = [
    "14\t1\t5\t1\t1\tإِيَّاكَ\tإياك\tإياك\tPRON\tStem\t\tDISJ_INV_PRON\tOBJ\tNOT_CONSTRUCT\tACCUSATIVE\tFATHA\t\t\tyou-alone",
    "15\t1\t5\t2\t1\tنَعْبُدُ\tنعبد\tن\tIMPERF_PREF\tPrefix\t\t\t\t\t\t\t\t\twe-worship",
    "15\t1\t5\t2\t2\tنَعْبُدُ\tنعبد\tعبد\tIV\tStem\t\tDECLN\tIV\tNOT_CONSTRUCT\tNOMINATIVE\tDHAMMA\t\t\twe-worship",
    "16\t1\t5\t3\t1\tوَإِيَّاكَ\tوإياك\tو\tCONJ\tPrefix\t\tINVAR\tCONJ\tNOT_CONSTRUCT\tINVARIABLE\tFATHA\t\t\tand-you-alone",
    "16\t1\t5\t3\t2\tوَإِيَّاكَ\tوإياك\tإياك\tPRON\tStem\t\tDISJ_INV_PRON\tOBJ\tNOT_CONSTRUCT\tACCUSATIVE\tFATHA\t\t\tand-you-alone",
    "17\t1\t5\t4\t1\tنَسْتَعِينُ\tنستعين\tن\tIMPERF_PREF\tPrefix\t\t\t\t\t\t\t\t\twe-ask-for-help",
    "17\t1\t5\t4\t2\tنَسْتَعِينُ\tنستعين\tستعين\tIV\tStem\t.\tDECLN\tIV\tNOT_CONSTRUCT\tNOMINATIVE\tDHAMMA\t\t\twe-ask-for-help",
    "20323\t7\t61\t1\t1\tقَالَ\tقال\tقال\tPV\tStem\t\tINVAR\t\tNOT_CONSTRUCT\tINVARIABLE\tFATHA\t\t\the-said",
    "20324\t7\t61\t2\t1\tيَا\tيا\tيا\tVOC_PART\tStem\t\tINVAR\tVOC_PART\tNOT_CONSTRUCT\tINVARIABLE\tSUKUN\t\t\to-my-people",
    "20325\t7\t61\t3\t1\tقَوْمِ\tقوم\tقوم\tNOUN_CONCRETE\tStem\t\tDECLN\tVOC\tCONSTRUCT\tACCUSATIVE\tIMP_FATHA\t\t\t(there-is)-no",
    "20325\t7\t61\t3\t2\tقَوْمِ\tقوم\t(null)\tCASE_DEF_GEN\tSuffix\t\tJONT_PRON\t\tNOT_CONSTRUCT\tGENITIVE\tSUKUN\t\t\t(there-is)-no",
]

# The app's Uthmani words (only the letters are compared, so mark order doesn't matter).
APP_1_5 = ["إِيَّاكَ", "نَعۡبُدُ", "وَإِيَّاكَ", "نَسۡتَعِينُ"]
APP_7_61_START = ["قَالَ", "يَٰقَوۡمِ"]


class MasaqTest(unittest.TestCase):
    def setUp(self):
        self.folder = tempfile.TemporaryDirectory()
        path = Path(self.folder.name) / MASAQ.file_name
        # The real file starts with a byte order mark.
        path.write_text("\ufeff" + HEADER + "\n" + "\n".join(ROWS) + "\n", encoding="utf-8")
        self.ayahs = masaq.read_words(path)

    def tearDown(self):
        self.folder.cleanup()

    def test_segments_are_grouped_into_words(self):
        words = self.ayahs[1005]
        self.assertEqual([len(word.segments) for word in words], [1, 2, 2, 2])
        self.assertEqual(words[1].gloss, "we-worship")
        self.assertEqual(words[1].segments[1]["Case_Mood"], "NOMINATIVE")

    def test_an_ayah_that_pairs_word_for_word(self):
        matches = masaq.align_ayah(APP_1_5, self.ayahs[1005])
        self.assertEqual([[word.word for word in match] for match in matches], [[1], [2], [3], [4]])

    def test_two_masaq_words_can_match_one_app_word(self):
        matches = masaq.align_ayah(APP_7_61_START, self.ayahs[7061])
        self.assertEqual([[word.text for word in match] for match in matches], [["قَالَ"], ["يَا", "قَوْمِ"]])

    def test_an_app_word_masaq_joins_to_the_previous_one_is_left_unmatched(self):
        # Everyday spelling writes وَحَيۡثُ مَا as one word, وَحَيْثُمَا.
        joined = [masaq.MasaqWord(2, 144, 15, "وَحَيْثُمَا", ""), masaq.MasaqWord(2, 144, 16, "كُنْتُمْ", "")]
        matches = masaq.align_ayah(["وَحَيۡثُ", "مَا", "كُنتُمۡ"], joined)
        self.assertEqual([[word.word for word in match] for match in matches], [[15], [], [16]])

    def test_spellings_differing_in_long_vowels_are_alike(self):
        self.assertGreaterEqual(masaq.similarity("ضَلَٰلَةٞ", "ضَلَالَةٌ"), 0.9)

    def test_only_ayahs_that_pair_word_for_word_are_regular(self):
        result = masaq.align(self.ayahs, {1005: APP_1_5, 7061: APP_7_61_START})
        self.assertEqual(result.regular_ayahs, {1005})
        self.assertEqual(result.unmatched_app_words, [])
        self.assertEqual(result.merged, 1)


class PackSyntaxTest(unittest.TestCase):
    def test_syntax_and_glosses_are_written(self):
        with tempfile.TemporaryDirectory() as folder:
            source = Path(folder) / MASAQ.file_name
            source.write_text(HEADER + "\n" + "\n".join(ROWS) + "\n", encoding="utf-8")
            result = masaq.align(masaq.read_words(source), {1005: APP_1_5, 7061: APP_7_61_START})
            path = Path(folder) / "learning_pack.db"
            pack.write(path, [], [], [], [MASAQ], {}, to_app, result.words, result.regular_ayahs)
            db = sqlite3.connect(path)
            try:
                glosses = db.execute("SELECT ayah_id, word_index, gloss FROM word_glosses").fetchall()
                vocative = db.execute(
                    "SELECT segment_index, text, role FROM syntax WHERE ayah_id = 7061 AND word_index = 1"
                ).fetchall()
            finally:
                db.close()
        # Glosses only for 1:5, hyphens turned into spaces. 7:61's glosses are shifted in MASAQ.
        self.assertEqual(glosses, [(1005, 0, "you alone"), (1005, 1, "we worship"),
                                   (1005, 2, "and you alone"), (1005, 3, "we ask for help")])
        # يا and قوم become segments 0-2 of the one app word يَٰقَوۡمِ.
        self.assertEqual([(index, role) for index, _, role in vocative], [(0, "VOC_PART"), (1, "VOC"), (2, None)])


if __name__ == "__main__":
    unittest.main()
