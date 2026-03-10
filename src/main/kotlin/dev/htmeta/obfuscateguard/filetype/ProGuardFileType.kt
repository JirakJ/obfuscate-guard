package dev.htmeta.obfuscateguard.filetype

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

/**
 * Language definition for ProGuard/R8 configuration files.
 */
class ProGuardLanguage private constructor() : Language("ProGuard") {
    companion object {
        @JvmField
        val INSTANCE = ProGuardLanguage()
    }

    override fun getDisplayName() = "ProGuard"
    override fun isCaseSensitive() = true
}

/**
 * File type for ProGuard configuration files (.pro, proguard-rules.pro).
 *
 * Extends [LanguageFileType] so IntelliJ can create a PSI tree, open the
 * file in the text editor, and apply syntax highlighting correctly.
 */
class ProGuardFileType private constructor() : LanguageFileType(ProGuardLanguage.INSTANCE) {
    override fun getName() = "ProGuard Config"
    override fun getDescription() = "ProGuard/R8 obfuscation configuration file"
    override fun getDefaultExtension() = "pro"
    override fun getIcon(): Icon = com.intellij.icons.AllIcons.FileTypes.Config

    companion object {
        @JvmField
        val INSTANCE = ProGuardFileType()
    }
}
