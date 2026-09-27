import unittest
from collections import Counter

from learning_pack import glosses


class NormaliseTest(unittest.TestCase):
    def test_verb_loses_its_subject_and_helpers(self):
        self.assertEqual(glosses.normalise("we worship", "V"), "worship")
        self.assertEqual(glosses.normalise("and they disbelieved", "V"), "disbelieved")
        self.assertEqual(glosses.normalise("indeed they will see", "V"), "see")

    def test_verb_loses_an_object_pronoun_at_the_end(self):
        self.assertEqual(glosses.normalise("he created them", "V"), "created")

    def test_noun_loses_articles_possessives_and_bracketed_words(self):
        self.assertEqual(glosses.normalise("(of) your lord", "N"), "lord")
        self.assertEqual(glosses.normalise("and the earth", "N"), "earth")
        self.assertEqual(glosses.normalise("in-(the)-name", "N"), "name")

    def test_particles_keep_their_meaning(self):
        # "not" is what لَا means, so it must survive.
        self.assertEqual(glosses.normalise("and not", "NEG"), "not")
        self.assertEqual(glosses.normalise("between them", "LOC"), "between")

    def test_nothing_is_stripped_to_empty(self):
        self.assertEqual(glosses.normalise("is", "V"), "is")


class DeriveTest(unittest.TestCase):
    def test_most_common_meaning_wins(self):
        meanings = glosses.derive(
            {"Eabada": Counter({"we worship": 5, "they worship": 4, "worship": 2, "you serve": 1})},
            {"Eabada": "V"},
        )
        self.assertEqual(meanings["Eabada"], "worship")

    def test_a_common_second_meaning_is_added(self):
        meanings = glosses.derive({"min": Counter({"from": 10, "of": 7, "among": 1})}, {"min": "P"})
        self.assertEqual(meanings["min"], "from, of")

    def test_a_second_meaning_that_repeats_the_first_is_not(self):
        meanings = glosses.derive({"Ean": Counter({"from": 10, "from them": 9})}, {"Ean": "P"})
        self.assertEqual(meanings["Ean"], "from")

    def test_names_are_capitalised(self):
        meanings = glosses.derive({"muwsaY`": Counter({"(to) musa": 3})}, {"muwsaY`": "PN"})
        self.assertEqual(meanings["muwsaY`"], "Musa")


if __name__ == "__main__":
    unittest.main()
