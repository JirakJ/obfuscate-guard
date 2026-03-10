package dev.htmeta.obfuscateguard.filetype

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

/**
 * Token types for ProGuard config lexer, bound to [ProGuardLanguage].
 */
object ProGuardTokenTypes {
    @JvmField val COMMENT = IElementType("PROGUARD_COMMENT", ProGuardLanguage.INSTANCE)
    @JvmField val DIRECTIVE = IElementType("PROGUARD_DIRECTIVE", ProGuardLanguage.INSTANCE)
    @JvmField val CLASS_PATTERN = IElementType("PROGUARD_CLASS_PATTERN", ProGuardLanguage.INSTANCE)
    @JvmField val WILDCARD = IElementType("PROGUARD_WILDCARD", ProGuardLanguage.INSTANCE)
    @JvmField val QUOTED_STRING = IElementType("PROGUARD_QUOTED_STRING", ProGuardLanguage.INSTANCE)
    @JvmField val TEXT = IElementType("PROGUARD_TEXT", ProGuardLanguage.INSTANCE)

    @JvmField val COMMENTS = TokenSet.create(COMMENT)
    @JvmField val STRINGS = TokenSet.create(QUOTED_STRING)
}

/**
 * Element type for ProGuard PSI file root.
 */
object ProGuardElementTypes {
    @JvmField val FILE = IElementType("PROGUARD_FILE", ProGuardLanguage.INSTANCE)
}
