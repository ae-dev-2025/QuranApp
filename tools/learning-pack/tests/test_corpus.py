import unittest

from learning_pack.corpus import group_words, parse_line, parse_segments

# Lines copied verbatim from the Quranic Arabic Corpus 0.4 (GNU GPL, corpus.quran.com),
# with the file's Windows line endings. Al-Fatihah 1:1 and 1:5, and 2:3 word 6.
FIXTURE = [
    "# a comment line is skipped\r\n",
    "LOCATION\tFORM\tTAG\tFEATURES\r\n",
    "(1:1:1:1)\tbi\tP\tPREFIX|bi+\r\n",
    "(1:1:1:2)\tsomi\tN\tSTEM|POS:N|LEM:{som|ROOT:smw|M|GEN\r\n",
    "(1:1:2:1)\t{ll~ahi\tPN\tSTEM|POS:PN|LEM:{ll~ah|ROOT:Alh|GEN\r\n",
    "(1:5:3:1)\twa\tCONJ\tPREFIX|w:CONJ+\r\n",
    "(1:5:3:2)\t<iy~aAka\tPRON\tSTEM|POS:PRON|LEM:<iy~aA|2MS\r\n",
    "(1:5:4:1)\tnasotaEiynu\tV\tSTEM|POS:V|IMPF|(X)|LEM:{sotaEiynu|ROOT:Ewn|1P\r\n",
    "(2:3:6:1)\twa\tREM\tPREFIX|w:REM+\r\n",
    "(2:3:6:2)\tmi\tP\tSTEM|POS:P|LEM:min\r\n",
    "(2:3:6:3)\tm~aA\tREL\tSTEM|POS:REL|LEM:maA\r\n",
]


class ParseLineTest(unittest.TestCase):
    def test_stem_values_and_flags(self):
        segment = parse_line(FIXTURE[3])
        self.assertEqual((segment.surah, segment.ayah, segment.word, segment.index), (1, 1, 1, 2))
        self.assertEqual(segment.kind, "STEM")
        self.assertEqual(segment.tag, "N")
        self.assertEqual(segment.lemma, "{som")
        self.assertEqual(segment.root, "smw")
        self.assertEqual(segment.flags, ("M", "GEN"))  # no trailing "\r"

    def test_prefix_with_colon_is_a_flag(self):
        segment = parse_line(FIXTURE[5])
        self.assertEqual(segment.kind, "PREFIX")
        self.assertEqual(segment.values, {})
        self.assertEqual(segment.flags, ("w:CONJ+",))

    def test_verb_form_and_person(self):
        segment = parse_line(FIXTURE[7])
        self.assertEqual(segment.values["POS"], "V")
        self.assertIn("(X)", segment.flags)
        self.assertIn("1P", segment.flags)

    def test_bad_location_is_rejected(self):
        with self.assertRaises(ValueError):
            parse_line("1:1:1:1\tbi\tP\tPREFIX|bi+")


class GroupWordsTest(unittest.TestCase):
    def setUp(self):
        self.words = group_words(parse_segments(FIXTURE))

    def test_segments_are_grouped_by_word(self):
        locations = [(word.surah, word.ayah, word.word) for word in self.words]
        self.assertEqual(locations, [(1, 1, 1), (1, 1, 2), (1, 5, 3), (1, 5, 4), (2, 3, 6)])
        self.assertEqual(self.words[0].form, "bisomi")

    def test_a_word_can_have_two_stems(self):
        # وَمِمَّا "and from what": two particles in one written word.
        stems = self.words[-1].stems
        self.assertEqual([stem.lemma for stem in stems], ["min", "maA"])

    def test_missing_segment_is_rejected(self):
        with self.assertRaises(ValueError):
            group_words(parse_segments([FIXTURE[2], FIXTURE[4].replace("(1:1:2:1)", "(1:1:2:2)")]))


if __name__ == "__main__":
    unittest.main()
