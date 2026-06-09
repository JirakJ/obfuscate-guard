package dev.htmeta.obfuscateguard.filetype

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement

/**
 * Quick documentation (Ctrl+Q / F1) for ProGuard directives in .pro files.
 */
class ProGuardDocumentationProvider : AbstractDocumentationProvider() {

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        val text = (originalElement ?: element)?.text?.trim() ?: return null
        if (!text.startsWith("-")) return null

        val directive = ProGuardDirectives.find(text) ?: return null

        return buildString {
            append("<b>${directive.name}</b>")
            if (directive.arguments.isNotBlank()) {
                append(" <i>${directive.arguments}</i>")
            }
            append("<br/><br/>")
            append(directive.description)
            if (directive.dangerous) {
                append("<br/><br/><b>⚠ Caution:</b> ${directive.dangerReason}")
            }
        }
    }
}
