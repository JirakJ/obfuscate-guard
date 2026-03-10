package dev.htmeta.obfuscateguard.filetype

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.lexer.Lexer
import com.intellij.psi.tree.IElementType

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

class ProGuardSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
        return ProGuardSyntaxHighlighter()
    }
}
