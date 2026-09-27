"""Rewrites non-ASCII characters in code lines (not comments) as unicode escapes, so
editors cannot reorder Arabic marks in test expectations. Usage: python escape_arabic.py FILE..."""
import sys

for path in sys.argv[1:]:
    lines = open(path, encoding="utf-8").read().split("\n")
    out = []
    for line in lines:
        if line.strip().startswith("#"):
            out.append(line)
        else:
            out.append("".join(c if ord(c) < 128 else chr(92) + "u%04x" % ord(c) for c in line))
    open(path, "w", encoding="utf-8", newline="\n").write("\n".join(out))
