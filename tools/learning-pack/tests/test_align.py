import unittest

from learning_pack.align import AlignmentError, align, skeleton
from learning_pack.buckwalter import to_unicode
from learning_pack.corpus import group_words, parse_segments

# Al-Fatihah 1:1 from the Quranic Arabic Corpus 0.4 (GNU GPL, corpus.quran.com).
CORPUS_1_1 = [
    "(1:1:1:1)\tbi\tP\tPREFIX|bi+\r\n",
    "(1:1:1:2)\tsomi\tN\tSTEM|POS:N|LEM:{som|ROOT:smw|M|GEN\r\n",
    "(1:1:2:1)\t{ll~ahi\tPN\tSTEM|POS:PN|LEM:{ll~ah|ROOT:Alh|GEN\r\n",
    "(1:1:3:1)\t{l\tDET\tPREFIX|Al+\r\n",
    "(1:1:3:2)\tr~aHoma`ni\tADJ\tSTEM|POS:ADJ|LEM:r~aHoma`n|ROOT:rHm|MS|GEN\r\n",
    "(1:1:4:1)\t{l\tDET\tPREFIX|Al+\r\n",
    "(1:1:4:2)\tr~aHiymi\tADJ\tSTEM|POS:ADJ|LEM:r~aHiym|ROOT:rHm|MS|GEN\r\n",
]

# The same ayah as the app stores it (only the bare letters are compared, so the order of
# marks inside these strings does not matter).
APP_1_1 = ["بِسۡمِ", "ٱللَّهِ", "ٱلرَّحۡمَٰنِ", "ٱلرَّحِيمِ"]


def corpus_words():
    return group_words(parse_segments(CORPUS_1_1))


class SkeletonTest(unittest.TestCase):
    def test_marks_are_dropped_and_wasl_is_folded(self):
        self.assertEqual(skeleton("ٱللَّهِ"), "الله")

    def test_hamza_before_alif_matches_alif_with_madda(self):
        # 2:4 وَبِٱلۡأٓخِرَةِ: the app writes أٓ, the corpus writes ءا.
        app = "\u0648\u064e\u0628\u0650\u0671\u0644\u06e1\u0623\u0653\u062e\u0650\u0631\u064e\u0629\u0650"
        self.assertEqual(skeleton(app), skeleton(to_unicode("wabi{lo'aAxirapi")))

    def test_signs_attached_to_a_word_are_ignored(self):
        # The app attaches ۞ (a quarter-hizb sign) to the first word of some ayahs.
        self.assertEqual(skeleton("۞قُلۡ"), skeleton(to_unicode("qulo")))


class AlignTest(unittest.TestCase):
    def test_words_pair_up_in_order(self):
        aligned = align(corpus_words(), {1001: APP_1_1}, to_unicode, expected_ayahs=1)
        self.assertEqual([word.word_index for word in aligned], [0, 1, 2, 3])
        self.assertEqual(aligned[1].corpus_word.stems[0].lemma, "{ll~ah")
        self.assertEqual(aligned[1].app_text, "ٱللَّهِ")

    def test_different_word_count_fails(self):
        with self.assertRaisesRegex(AlignmentError, "1001: app 3 words, corpus 4"):
            align(corpus_words(), {1001: APP_1_1[:3]}, to_unicode, expected_ayahs=1)

    def test_different_letters_fail(self):
        swapped = APP_1_1[:3] + ["ٱلۡعَٰلَمِينَ"]
        with self.assertRaisesRegex(AlignmentError, "1001:3"):
            align(corpus_words(), {1001: swapped}, to_unicode, expected_ayahs=1)

    def test_missing_ayahs_fail(self):
        with self.assertRaisesRegex(AlignmentError, "ayahs"):
            align(corpus_words(), {1001: APP_1_1}, to_unicode)


if __name__ == "__main__":
    unittest.main()
