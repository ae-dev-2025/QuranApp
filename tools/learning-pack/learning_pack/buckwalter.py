"""Converts the corpus's extended Buckwalter transliteration to Arabic script.

The corpus writes Arabic in ASCII, one character per Arabic letter or mark
(https://corpus.quran.com/java/buckwalter.jsp). For example `{ll~ahi` is ٱللَّهِ.

Two outputs are needed because the app's Quran font uses a few code points differently
from Unicode's names for them:

- `to_unicode` follows the corpus (and Tanzil): U+0652 is the sukun and U+06DF is the
  small circle over a silent letter.
- `to_app` follows the app's Uthmani text and its Hafs font: U+06E1 is the sukun and
  U+0652 is the silent-letter circle (see `learning/analysis/Arabic.kt`).
"""

_TABLE = {
    "'": "ء",  # hamza
    ">": "أ",  # alif with hamza above
    "&": "ؤ",  # waw with hamza above
    "<": "إ",  # alif with hamza below
    "}": "ئ",  # yaa with hamza above
    "A": "ا",  # alif
    "b": "ب",
    "p": "ة",  # taa marbuta
    "t": "ت",
    "v": "ث",
    "j": "ج",
    "H": "ح",
    "x": "خ",
    "d": "د",
    "*": "ذ",
    "r": "ر",
    "z": "ز",
    "s": "س",
    "$": "ش",
    "S": "ص",
    "D": "ض",
    "T": "ط",
    "Z": "ظ",
    "E": "ع",
    "g": "غ",
    "_": "ـ",  # tatweel
    "f": "ف",
    "q": "ق",
    "k": "ك",
    "l": "ل",
    "m": "م",
    "n": "ن",
    "h": "ه",
    "w": "و",
    "Y": "ى",  # alif maqsura
    "y": "ي",
    "F": "ً",  # fathatan
    "N": "ٌ",  # dammatan
    "K": "ٍ",  # kasratan
    "a": "َ",  # fatha
    "u": "ُ",  # damma
    "i": "ِ",  # kasra
    "~": "ّ",  # shadda
    "o": "ْ",  # sukun
    "^": "ٓ",  # maddah above
    "#": "ٔ",  # hamza above
    "`": "ٰ",  # dagger alif
    "{": "ٱ",  # hamzat al-wasl
    ":": "ۜ",  # small high seen
    "@": "۟",  # small high rounded zero (silent letter)
    '"': "۠",  # small high upright rectangular zero
    "[": "ۢ",  # small high meem (iqlab)
    ";": "ۣ",  # small low seen
    ",": "ۥ",  # small waw
    ".": "ۦ",  # small yaa
    "!": "ۨ",  # small high noon
    "-": "۪",  # empty centre low stop
    "+": "۫",  # empty centre high stop
    "%": "۬",  # rounded high stop with filled centre
    "]": "ۭ",  # small low meem
    " ": " ",
}

# Where the app's text differs from Unicode's naming (see the module docstring).
_APP_OVERRIDES = {
    "o": "ۡ",
    "@": "ْ",
}


def to_unicode(buckwalter: str) -> str:
    return "".join(_convert(char, {}) for char in buckwalter)


def to_app(buckwalter: str) -> str:
    return "".join(_convert(char, _APP_OVERRIDES) for char in buckwalter)


def _convert(char: str, overrides: dict) -> str:
    if char in overrides:
        return overrides[char]
    try:
        return _TABLE[char]
    except KeyError:
        raise ValueError(f"not an extended Buckwalter character: {char!r}") from None
