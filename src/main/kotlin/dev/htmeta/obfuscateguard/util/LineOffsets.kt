package dev.htmeta.obfuscateguard.util

/** Offsets of the first non-blank character of each line, computed once per text. */
class LineOffsets(private val text: String) {
    private val starts: IntArray = buildList {
        add(0)
        text.forEachIndexed { i, c -> if (c == '\n') add(i + 1) }
    }.toIntArray()

    val lineCount: Int get() = starts.size

    /**
     * Offset of the first non-whitespace character on [lineIndex] (or the line end for blank lines),
     * clamped to a valid offset in [text]; `-1` for an empty text or an out-of-range line.
     */
    fun firstNonBlank(lineIndex: Int): Int {
        if (text.isEmpty() || lineIndex !in starts.indices) return -1
        var offset = starts[lineIndex]
        while (offset < text.length && text[offset] != '\n' && text[offset].isWhitespace()) offset++
        return offset.coerceAtMost(text.length - 1)
    }
}
