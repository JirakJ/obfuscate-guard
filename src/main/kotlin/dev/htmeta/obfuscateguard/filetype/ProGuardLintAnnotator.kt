package dev.htmeta.obfuscateguard.filetype

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/**
 * Editor annotations for ProGuard config files: unknown directives with
 * typo suggestions, dangerous directives, and duplicates.
 */
class ProGuardLintAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Annotate once per file, on the file element itself
        if (element !is PsiFile) return
        if (element.language != ProGuardLanguage.INSTANCE) return

        for (issue in ProGuardConfigLinter.lint(element.text)) {
            val severity = when (issue.severity) {
                ProGuardConfigLinter.IssueSeverity.ERROR -> HighlightSeverity.ERROR
                ProGuardConfigLinter.IssueSeverity.WARNING -> HighlightSeverity.WARNING
                ProGuardConfigLinter.IssueSeverity.WEAK_WARNING -> HighlightSeverity.WEAK_WARNING
            }
            holder.newAnnotation(severity, "ObfuscateGuard: ${issue.message}")
                .range(TextRange(issue.startOffset, issue.endOffset))
                .create()
        }
    }
}
