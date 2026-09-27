package com.quranapp.android.learning.pack

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * The tables of learning_pack.db, the downloadable data behind the Words and Grammar layers.
 * The file is built by tools/learning-pack, whose SCHEMA must match these entities exactly:
 * Room refuses a database whose tables differ. A pipeline test compares the two.
 *
 * Word positions use the app's own numbering: ayah_id = surah * 1000 + ayah, and word_index
 * counts from 0, as in quranapp.db.
 */

/** Settings of the pack itself, such as `pack_version`. */
@Entity(tableName = "meta")
data class PackMetaEntity(
    @PrimaryKey
    @ColumnInfo(name = "key")
    val key: String,
    @ColumnInfo(name = "value")
    val value: String,
)

/** A data source, with the licence and notice the app must show. */
@Entity(tableName = "credits")
data class PackCreditEntity(
    @PrimaryKey
    @ColumnInfo(name = "credit_id")
    val creditId: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "licence")
    val licence: String,
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "notice")
    val notice: String,
)

/** A root such as ع ب د, with how often words of this root occur in the Quran. */
@Entity(tableName = "roots", indices = [Index(value = ["root_key"], unique = true)])
data class RootEntity(
    @PrimaryKey
    @ColumnInfo(name = "root_id")
    val rootId: Int,
    /** The corpus's own spelling (`Ebd`). Stable across pack versions, unlike the id. */
    @ColumnInfo(name = "root_key")
    val rootKey: String,
    /** Arabic letters with spaces: "ع ب د". */
    @ColumnInfo(name = "letters")
    val letters: String,
    @ColumnInfo(name = "occurrences")
    val occurrences: Int,
)

/** A lemma: the dictionary word a learner memorises, such as عَبَدَ "to worship". */
@Entity(
    tableName = "lemmas",
    indices = [Index(value = ["lemma_key"], unique = true), Index(value = ["root_id"])],
)
data class LemmaEntity(
    @PrimaryKey
    @ColumnInfo(name = "lemma_id")
    val lemmaId: Int,
    /** The corpus's own spelling (`Eabada`). Progress is stored by this, never by the id. */
    @ColumnInfo(name = "lemma_key")
    val lemmaKey: String,
    /** How a dictionary writes it, in the app's Quran font (عَبَدَ). */
    @ColumnInfo(name = "headword")
    val headword: String,
    /** `corpus`, `quran`, `pattern` or `unchecked`: where the headword came from. */
    @ColumnInfo(name = "headword_source")
    val headwordSource: String,
    /** The corpus's part-of-speech tag, such as `N`, `V` or `P`. */
    @ColumnInfo(name = "pos")
    val pos: String,
    @ColumnInfo(name = "root_id")
    val rootId: Int?,
    /** "I" to "XII" for verbs, null otherwise. */
    @ColumnInfo(name = "verb_form")
    val verbForm: String?,
    @ColumnInfo(name = "occurrences")
    val occurrences: Int,
    /** An English dictionary meaning, where one has been written. */
    @ColumnInfo(name = "gloss")
    val gloss: String?,
)

/** A prefix, stem or suffix of a word, from the Quranic Arabic Corpus. */
@Entity(
    tableName = "segments",
    primaryKeys = ["ayah_id", "word_index", "segment_index"],
    indices = [Index(value = ["lemma_id"])],
)
data class SegmentEntity(
    @ColumnInfo(name = "ayah_id")
    val ayahId: Int,
    @ColumnInfo(name = "word_index")
    val wordIndex: Int,
    @ColumnInfo(name = "segment_index")
    val segmentIndex: Int,
    /** The segment in Arabic, such as وَ or إِيَّاكَ. */
    @ColumnInfo(name = "form")
    val form: String,
    /** `PREFIX`, `STEM` or `SUFFIX`. */
    @ColumnInfo(name = "kind")
    val kind: String,
    /** Part of speech, such as `N`, `V`, `P` or `CONJ`. */
    @ColumnInfo(name = "tag")
    val tag: String,
    /** Set for stems only. */
    @ColumnInfo(name = "lemma_id")
    val lemmaId: Int?,
    /** The corpus's features joined by "|", such as `IMPF|(X)|1P` or `PRON:2MS`. */
    @ColumnInfo(name = "features")
    val features: String,
)

/** A word segment's role in its sentence (iʿrāb), from MASAQ. */
@Entity(tableName = "syntax", primaryKeys = ["ayah_id", "word_index", "segment_index"])
data class SyntaxEntity(
    @ColumnInfo(name = "ayah_id")
    val ayahId: Int,
    @ColumnInfo(name = "word_index")
    val wordIndex: Int,
    @ColumnInfo(name = "segment_index")
    val segmentIndex: Int,
    @ColumnInfo(name = "text")
    val text: String,
    @ColumnInfo(name = "morph_tag")
    val morphTag: String,
    @ColumnInfo(name = "morph_type")
    val morphType: String,
    @ColumnInfo(name = "declinability")
    val declinability: String?,
    /** Such as `SUBJ`, `OBJ`, `GEN_CONS` (second noun of an iḍāfa) or `PREP_OBJ`. */
    @ColumnInfo(name = "role")
    val role: String?,
    /** `CONSTRUCT` when this is the first noun of an iḍāfa. */
    @ColumnInfo(name = "construct")
    val construct: String?,
    /** `NOMINATIVE`, `ACCUSATIVE`, `GENITIVE`, `JUSSIVE` or `INVARIABLE`. */
    @ColumnInfo(name = "case_mood")
    val caseMood: String?,
    /** The ending that shows the case or mood, such as `DHAMMA` or `YAA`. */
    @ColumnInfo(name = "case_marker")
    val caseMarker: String?,
    @ColumnInfo(name = "phrase")
    val phrase: String?,
    @ColumnInfo(name = "phrase_function")
    val phraseFunction: String?,
)

/** An English gloss of one word in its context ("we worship"), from MASAQ. */
@Entity(tableName = "word_glosses", primaryKeys = ["ayah_id", "word_index"])
data class WordGlossEntity(
    @ColumnInfo(name = "ayah_id")
    val ayahId: Int,
    @ColumnInfo(name = "word_index")
    val wordIndex: Int,
    @ColumnInfo(name = "gloss")
    val gloss: String,
)
