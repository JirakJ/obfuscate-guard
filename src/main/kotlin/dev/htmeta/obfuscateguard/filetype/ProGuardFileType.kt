package dev.htmeta.obfuscateguard.filetype

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.lexer.Lexer
import com.intellij.psi.tree.IElementType
import javax.swing.Icon

/**
 * File type for ProGuard configuration files (.pro, proguard-rules.pro).
 */
class ProGuardFileType : FileType {
    override fun getName() = "ProGuard Config"
    override fun getDescription() = "ProGuard/R8 obfuscation configuration file"
    override fun getDefaultExtension() = "pro"
    override fun getIcon(): Icon = com.intellij.icons.AllIcons.FileTypes.Config
    override fun isBinary() = false
    override fun isReadOnly() = false

    companion object {
        val INSTANCE = ProGuardFileType()
    }
}

object ProGuardTokenTypes {
    val COMMENT = IElementType("PROGUARD_COMMENT", null)
    val DIRECTIVE = IElementType("PROGUARD_DIRECTIVE", null)
    val CLASS_PATTERN = IElementType("PROGUARD_CLASS_PATTERN", null)
    val WILDCARD = IElementType("PROGUARD_WILDCARD", null)
    val QUOTED_STRING = IElementType("PROGUARD_QUOTED_STRING", null)
    val TEXT = IElementType("PROGUARD_TEXT", null)
}

/**
 * Syntax highlighter for ProGuard config files.
 */
class ProGuardSyntaxHighlighter : SyntaxHighlighter {

    companion object {
        val COMMENT = TextAttributesKey.createTextAttributesKey("PROGUARD_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val DIRECTIVE = TextAttributesKey.createTextAttributesKey("PROGUARD_DIRECTIVE", DefaultLanguageHighlighterColors.KEYWORD)
        val CLASS_NAME = TextAttributesKey.createTextAttributesKey("PROGUARD_CLASS", DefaultLanguageHighlighterColors.CLASS_NAME)
        val WILDCARD = TextAttributesKey.createTextAttributesKey("PROGUARD_WILDCARD", DefaultLanguageHighlighterColors.NUMBER)
        val STRING = TextAttributesKey.createTextAttributesKey("PROGUARD_STRING", DefaultLanguageHighlighterColors.STRING)
    }

    override fun getHighlightingLexer(): Lexer = ProGuardLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = when (tokenType) {
        ProGuardTokenTypes.COMMENT -> arrayOf(COMMENT)
        ProGuardTokenTypes.DIRECTIVE -> arrayOf(DIRECTIVE)
        ProGuardTokenTypes.CLASS_PATTERN -> arrayOf(CLASS_NAME)
        ProGuardTokenTypes.WILDCARD -> arrayOf(WILDCARD)
        ProGuardTokenTypes.QUOTED_STRING -> arrayOf(STRING)
        else -> emptyArray()
    }
}

/**
 * Simple lexer for ProGuard config syntax.
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

    override fun getCurrentPosition(): com.intellij.lexer.LexerPosition {
        val s = tokenStart
        val st = state
        return object : com.intellij.lexer.LexerPosition {
            override fun getOffset() = s
            override fun getState() = st
        }
    }

    override fun restore(position: com.intellij.lexer.LexerPosition) {
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
        while (i < endOffset && (buffer[i].isLetterOrDigit() || buffer[i] == '.' || buffer[i] == '$' || buffer[i] == '_' || buffer[i] == '*')) i++
        return (i - from) > 3 && buffer.subSequence(from, i).contains('.')
    }
}

class ProGuardSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
        return ProGuardSyntaxHighlighter()
    }
}
