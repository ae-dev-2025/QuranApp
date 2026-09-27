import unittest

from learning_pack.buckwalter import to_app, to_unicode

# Expected Arabic is written as escapes: editors often normalise "shadda + vowel" into the
# other order, which would silently change what these tests check.


class BuckwalterTest(unittest.TestCase):
    def test_name_of_allah(self):
        # ٱللَّهِ
        self.assertEqual(to_unicode("{ll~ahi"), "\u0671\u0644\u0644\u0651\u064e\u0647\u0650")

    def test_sukun_differs_between_unicode_and_the_app(self):
        # نَعۡبُدُ: the corpus uses U+0652 for the sukun, the app's text U+06E1.
        self.assertEqual(to_unicode("naEobudu"), "\u0646\u064e\u0639\u0652\u0628\u064f\u062f\u064f")
        self.assertEqual(to_app("naEobudu"), "\u0646\u064e\u0639\u06e1\u0628\u064f\u062f\u064f")

    def test_silent_letter_circle(self):
        # The rounded zero marks a silent letter: U+06DF in Unicode, U+0652 in the app.
        self.assertEqual(to_unicode("A@"), "\u0627\u06df")
        self.assertEqual(to_app("A@"), "\u0627\u0652")

    def test_dagger_alif_and_small_letters(self):
        # رَّحْمَٰنِ and بِهِۦ
        self.assertEqual(
            to_unicode("r~aHoma`ni"),
            "\u0631\u0651\u064e\u062d\u0652\u0645\u064e\u0670\u0646\u0650",
        )
        self.assertEqual(to_unicode("bihi."), "\u0628\u0650\u0647\u0650\u06e6")

    def test_space_is_kept(self):
        # إِلْ يَاسِينَ (37:130)
        self.assertEqual(
            to_unicode("<ilo yaAsiyna"),
            "\u0625\u0650\u0644\u0652 \u064a\u064e\u0627\u0633\u0650\u064a\u0646\u064e",
        )

    def test_unknown_character_is_rejected(self):
        with self.assertRaises(ValueError):
            to_unicode("c")


if __name__ == "__main__":
    unittest.main()
