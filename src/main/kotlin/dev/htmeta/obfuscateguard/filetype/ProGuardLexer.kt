package dev.htmeta.obfuscateguard.filetype

import com.intellij.lexer.Lexer
import com.intellij.lexer.LexerPosition
import com.intellij.psi.tree.IElementType

/**
 * Simple lexer for ProGuard config syntax.
 *
 * Tokenizes comments (#), directives (-keep, -dontwarn, etc.),
 * class patterns (com.example.Foo), wildcards (*, **), and quoted strings.
 */
class ProGuardLexer : Lexer() {

    private var buffer: CharSequence = ""
    private var startOffset = 0
    private var endOffset = 0
    private var tokenStart = 0
    private var tokenEnd = 0
    private var tokenType: IElementType? = null

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.startOffset = startOffset
        this.endOffset = endOffset
        this.tokenStart = startOffset
        this.tokenEnd = startOffset
        advance()
    }

    override fun getState() = 0
    override fun getTokenType() = tokenType
    override fun getTokenStart() = tokenStart
    override fun getTokenEnd() = tokenEnd
    override fun getBufferSequence() = buffer
    override fun getBufferEnd() = endOffset

    override fun getCurrentPosition(): LexerPosition {
        val s = tokenStart
        val st = state
        return object : LexerPosition {
            override fun getOffset() = s
            override fun getState() = st
        }
    }

    override fun restore(position: LexerPosition) {
        tokenEnd = position.offset
        advance()
    }

    override fun advance() {
        tokenStart = tokenEnd
        if (tokenStart >= endOffset) {
            tokenType = null
            return
        }

        val c = buffer[tokenStart]

        when {
            c == '#' -> {
                tokenEnd = findLineEnd(tokenStart)
                tokenType = ProGuardTokenTypes.COMMENT
            }
            c == '-' -> {
                tokenEnd = findWordEnd(tokenStart)
                tokenType = ProGuardTokenTypes.DIRECTIVE
            }
            c == '"' -> {
                tokenEnd = findQuoteEnd(tokenStart)
                tokenType = ProGuardTokenTypes.QUOTED_STRING
            }
            c == '*' || (c == '<' && tokenStart + 1 < endOffset) -> {
                tokenEnd = findWordEnd(tokenStart)
                tokenType = ProGuardTokenTypes.WILDCARD
            }
            c.isLetter() && isClassPattern(tokenStart) -> {
                tokenEnd = findWordEnd(tokenStart)
                tokenType = ProGuardTokenTypes.CLASS_PATTERN
            }
            else -> {
                tokenEnd = findWordEnd(tokenStart)
                tokenType = ProGuardTokenTypes.TEXT
            }
        }
    }

    private fun findLineEnd(from: Int): Int {
        var i = from
        while (i < endOffset && buffer[i] != '\n') i++
        return if (i < endOffset) i + 1 else i
    }

    private fun findWordEnd(from: Int): Int {
        var i = from + 1
        while (i < endOffset && !buffer[i].isWhitespace()) i++
        return i
    }

    private fun findQuoteEnd(from: Int): Int {
        var i = from + 1
        while (i < endOffset && buffer[i] != '"') i++
        return if (i < endOffset) i + 1 else i
    }

    private fun isClassPattern(from: Int): Boolean {
        var i = from
        while (i < endOffset && isClassPatternChar(buffer[i])) i++
        return (i - from) > 3 && buffer.subSequence(from, i).contains('.')
    }

    private fun isClassPatternChar(c: Char): Boolean = c.isLetterOrDigit() || c in CLASS_PATTERN_SYMBOLS

    private companion object {
        const val CLASS_PATTERN_SYMBOLS = ".\$_*"
    }
}
