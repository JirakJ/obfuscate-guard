package dev.htmeta.obfuscateguard.filetype

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.PsiPlainTextFileImpl
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.lang.PsiBuilder

/**
 * Parser definition for ProGuard configuration files.
 *
 * Provides a minimal parser that wraps the entire file content into a single
 * root node. This is sufficient for syntax highlighting, code folding, and
 * basic editor support without full semantic parsing.
 */
class ProGuardParserDefinition : ParserDefinition {

    companion object {
        @JvmField
        val FILE = IFileElementType(ProGuardLanguage.INSTANCE)
    }

    override fun createLexer(project: Project?): Lexer = ProGuardLexer()

    override fun createParser(project: Project?): PsiParser = ProGuardParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = ProGuardTokenTypes.COMMENTS

    override fun getStringLiteralElements(): TokenSet = ProGuardTokenTypes.STRINGS

    override fun createElement(node: ASTNode): PsiElement {
        return com.intellij.psi.impl.source.tree.LeafPsiElement(node.elementType, node.text)
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return ProGuardFile(viewProvider)
    }
}

/**
 * Minimal parser that consumes all tokens into a flat root node.
 */
class ProGuardParser : PsiParser {
    override fun parse(root: com.intellij.psi.tree.IElementType, builder: PsiBuilder): ASTNode {
        val rootMarker = builder.mark()
        while (!builder.eof()) {
            builder.advanceLexer()
        }
        rootMarker.done(root)
        return builder.treeBuilt
    }
}

/**
 * PSI file for ProGuard configuration files.
 */
class ProGuardFile(viewProvider: FileViewProvider) :
    com.intellij.extapi.psi.PsiFileBase(viewProvider, ProGuardLanguage.INSTANCE) {

    override fun getFileType() = ProGuardFileType.INSTANCE

    override fun toString() = "ProGuard Config File"
}
