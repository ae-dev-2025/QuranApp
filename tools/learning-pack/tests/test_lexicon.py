import unittest

from learning_pack import lexicon
from learning_pack.corpus import group_words, parse_segments


def words_from(stems):
    """One-segment words from (buckwalter form, features) pairs, one ayah, in order."""
    lines = [
        # The tag column repeats the POS feature, as in the corpus.
        f"(1:1:{index}:1)\t{form}\t{features.split('|')[0].split(':')[1]}\tSTEM|{features}\r\n"
        for index, (form, features) in enumerate(stems, start=1)
    ]
    return group_words(parse_segments(lines))


def lemma(lemmas, key):
    return next(item for item in lemmas if item.key == key)


class HeadwordTest(unittest.TestCase):
    def test_a_past_tense_lemma_is_kept(self):
        _, lemmas = lexicon.build(words_from([("naEobudu", "POS:V|IMPF|LEM:Eabada|ROOT:Ebd|1P")]))
        self.assertEqual(lemma(lemmas, "Eabada").headword_source, "corpus")
        self.assertEqual(lemma(lemmas, "Eabada").headword, lexicon.display("Eabada"))

    def test_he_did_form_from_the_quran_replaces_an_inflected_lemma(self):
        _, lemmas = lexicon.build(words_from([
            ("yaxodaEu", "POS:V|IMPF|LEM:yaxodaEu|ROOT:xdE|3MS"),
            ("xadaEa", "POS:V|PERF|LEM:yaxodaEu|ROOT:xdE|3MS"),
        ]))
        self.assertEqual(lemma(lemmas, "yaxodaEu").headword_source, "quran")
        self.assertEqual(lemma(lemmas, "yaxodaEu").headword, lexicon.display("xadaEa"))

    def test_form_one_takes_its_middle_vowel_from_another_past_form(self):
        _, lemmas = lexicon.build(words_from([
            ("yaEoqiluwna", "POS:V|IMPF|LEM:Eaqalu|ROOT:Eql|3MP"),
            ("Eaqaluwhu", "POS:V|PERF|LEM:Eaqalu|ROOT:Eql|3MP"),
        ]))
        self.assertEqual(lemma(lemmas, "Eaqalu").headword_source, "pattern")
        self.assertEqual(lemma(lemmas, "Eaqalu").headword, lexicon.display("Eaqala"))

    def test_derived_forms_are_built_from_the_root(self):
        _, lemmas = lexicon.build(words_from([
            ("nasotaEiynu", "POS:V|IMPF|(X)|LEM:{sotaEiynu|ROOT:Ewn|1P"),  # hollow root
            ("yuHibbu", "POS:V|IMPF|(IV)|LEM:>aHobabo|ROOT:Hbb|3MS"),  # doubled root
            ("yuzak~iy", "POS:V|IMPF|(II)|LEM:yuzak~iy|ROOT:zkw|3MS"),  # defective root
            ("tatafak~aruwna", "POS:V|IMPF|(V)|LEM:yatafak~aru|ROOT:fkr|2MP"),  # sound root
        ]))
        self.assertEqual(lemma(lemmas, "{sotaEiynu").headword, lexicon.display("{sotaEaAna"))
        self.assertEqual(lemma(lemmas, ">aHobabo").headword, lexicon.display(">aHab~a"))
        self.assertEqual(lemma(lemmas, "yuzak~iy").headword, lexicon.display("zak~aY`"))
        self.assertEqual(lemma(lemmas, "yatafak~aru").headword, lexicon.display("tafak~ara"))

    def test_a_lemma_that_occurs_as_he_did_is_trusted(self):
        # تَعَٰلَىٰ is tagged form I, but the lemma itself occurs as "he did".
        _, lemmas = lexicon.build(words_from([("taEa`laY`", "POS:V|PERF|LEM:taEa`laY`|ROOT:Elw|3MS")]))
        self.assertEqual(lemma(lemmas, "taEa`laY`").headword_source, "corpus")

    def test_a_verb_without_evidence_is_left_for_a_person(self):
        _, lemmas = lexicon.build(words_from([("ya*aru", "POS:V|IMPF|LEM:ya*aru|ROOT:w*r|3MS")]))
        self.assertEqual(lemma(lemmas, "ya*aru").headword_source, "unchecked")
        self.assertEqual(lemma(lemmas, "ya*aru").headword, lexicon.display("ya*aru"))


class LexiconTest(unittest.TestCase):
    def test_counts_ids_and_roots(self):
        roots, lemmas = lexicon.build(words_from([
            ("qaAla", "POS:V|PERF|LEM:qaAla|ROOT:qwl|3MS"),
            ("qaAla", "POS:V|PERF|LEM:qaAla|ROOT:qwl|3MS"),
            ("{som", "POS:N|LEM:{som|ROOT:smw|M|NOM"),
        ]))
        self.assertEqual([(item.key, item.lemma_id, item.occurrences) for item in lemmas],
                         [("qaAla", 1, 2), ("{som", 2, 1)])
        self.assertEqual(lemma(lemmas, "{som").pos, "N")
        self.assertIsNone(lemma(lemmas, "{som").verb_form)
        self.assertEqual([(root.key, root.letters) for root in roots], [("qwl", "\u0642 \u0648 \u0644"), ("smw", "\u0633 \u0645 \u0648")])

    def test_hamza_radical_shows_as_hamza(self):
        self.assertEqual(lexicon.root_letters("Amn"), "\u0621 \u0645 \u0646")

    def test_digit_telling_homographs_apart_is_hidden(self):
        self.assertEqual(lexicon.display("maE2"), lexicon.display("maE"))


if __name__ == "__main__":
    unittest.main()


class HeadwordCleanupTest(unittest.TestCase):
    def test_a_doubled_first_letter_loses_its_shadda(self):
        # نَّاس (from ٱلنَّاس) is written نَاس in a dictionary.
        self.assertEqual(lexicon.display("n~aAs"), lexicon.display("naAs"))

    def test_nouns_ending_in_short_at_get_a_taa_marbuta(self):
        self.assertEqual(lexicon.headword_of("EibaAdat", "N"), "EibaAdap")
        self.assertEqual(lexicon.headword_of("Sa`liHa`t", "N"), "Sa`liHa`t")  # a real plural in -aat
        self.assertEqual(lexicon.headword_of("Eanat", "N"), "Eanat")  # hardship really ends in taa
        self.assertEqual(lexicon.headword_of("xalaqat", "V"), "xalaqat")  # only nouns change

    def test_a_bare_pronoun_gets_a_lemma_by_person(self):
        roots, lemmas = lexicon.build(words_from([
            ("huwa", "POS:PRON|3MS"),
            ("hu,", "POS:PRON|3MS"),
        ]))
        pronoun = lemma(lemmas, "PRON:3MS")
        self.assertEqual(pronoun.occurrences, 2)
        self.assertEqual(pronoun.headword_source, "pronoun")
        self.assertEqual(pronoun.headword, lexicon.display("huwa"))
