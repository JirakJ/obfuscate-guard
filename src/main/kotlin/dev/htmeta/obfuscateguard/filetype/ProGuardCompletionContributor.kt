package dev.htmeta.obfuscateguard.filetype

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext

/**
 * Code completion for ProGuard directives in .pro files.
 * Triggered when typing '-' at the start of a directive.
 */
class ProGuardCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(ProGuardLanguage.INSTANCE),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet,
                ) {
                    for (directive in ProGuardDirectives.ALL) {
                        result.addElement(
                            LookupElementBuilder.create(directive.name)
                                .withTypeText(directive.arguments.ifBlank { "no arguments" }, true)
                                .withTailText("  ${directive.description.take(60)}", true)
                        )
                    }
                }
            },
        )
    }
}
