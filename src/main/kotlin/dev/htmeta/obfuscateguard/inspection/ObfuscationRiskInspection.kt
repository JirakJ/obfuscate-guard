package dev.htmeta.obfuscateguard.inspection

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile

/**
 * IntelliJ inspection that detects obfuscation risks inline in the editor.
 * Shows warnings in the Problems view and enables quick-fix actions.
 */
class ObfuscationRiskInspection : LocalInspectionTool() {

    override fun getGroupDisplayName() = "ObfuscateGuard"
    override fun getDisplayName() = "Obfuscation risk detection"
    override fun getShortName() = "ObfuscationRisk"
    override fun isEnabledByDefault() = true

    private data class RiskPattern(
        val regex: Regex,
        val message: String,
        val highlightType: ProblemHighlightType,
    )

    private val patterns = listOf(
        RiskPattern(
            Regex("""Class\.forName\s*\("""),
            "Class.forName() — class name will change after obfuscation. Add a keep rule.",
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
        ),
        RiskPattern(
            Regex("""\.(getDeclaredMethod|getDeclaredField)\s*\("""),
            "Reflective access — member name will change after obfuscation.",
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
        ),
        RiskPattern(
            Regex("""\bnative\s+\w+\s+\w+\s*\("""),
            "Native method — JNI binding uses exact method name. Must be kept.",
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
        ),
        RiskPattern(
            Regex("""@SerializedName|@JsonProperty|@SerialName"""),
            "Serialization annotation — field mapping depends on name. Verify keep rules.",
            ProblemHighlightType.WARNING,
        ),
        RiskPattern(
            Regex("""@(Component|Service|Repository|Controller|Inject|Named)"""),
            "DI framework annotation — class/field discovered by name at runtime.",
            ProblemHighlightType.WARNING,
        ),
        RiskPattern(
            Regex("""ServiceLoader\.load\s*\("""),
            "ServiceLoader — META-INF/services uses fully qualified class names.",
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
        ),
        RiskPattern(
            Regex("""@Entity|@Table|@Column"""),
            "JPA/ORM entity — class and field names used by persistence framework.",
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
        ),
    )

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                val ext = file.virtualFile?.extension ?: return
                if (ext !in setOf("java", "kt", "kts")) return

                val text = file.text
                val lines = text.lines()

                for (pattern in patterns) {
                    for ((lineIndex, line) in lines.withIndex()) {
                        if (pattern.regex.containsMatchIn(line)) {
                            val offset = lineOffset(text, lineIndex)
                            val element = file.findElementAt(offset) ?: continue

                            holder.registerProblem(
                                element,
                                "ObfuscateGuard: ${pattern.message}",
                                pattern.highlightType,
                                AddKeepRuleQuickFix(element),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun lineOffset(text: String, lineIndex: Int): Int {
        var offset = 0
        for (i in 0 until lineIndex) {
            offset = text.indexOf('\n', offset) + 1
            if (offset == 0) return text.length - 1
        }
        while (offset < text.length && text[offset] == ' ') offset++
        return offset.coerceAtMost(text.length - 1)
    }
}

/**
 * Quick-fix: generates a keep rule for the detected risk.
 */
class AddKeepRuleQuickFix(element: PsiElement) : LocalQuickFix {

    private val className: String = extractClassName(element)

    override fun getName() = "Add ObfuscateGuard keep rule for '$className'"
    override fun getFamilyName() = "ObfuscateGuard"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val service = dev.htmeta.obfuscateguard.core.ObfuscateGuardProjectService.getInstance(project)
        val config = service.config ?: dev.htmeta.obfuscateguard.core.model.ObfuscationConfig()

        val newRule = dev.htmeta.obfuscateguard.core.model.KeepRule(
            pattern = "class $className { *; }",
            reason = "Added via quick-fix from inspection",
            needsReview = false,
            accepted = true,
        )

        if (config.keepRules.none { it.pattern == newRule.pattern }) {
            service.updateConfig(config.copy(keepRules = config.keepRules + newRule))

            com.intellij.notification.NotificationGroupManager.getInstance()
                .getNotificationGroup("ObfuscateGuard")
                .createNotification(
                    "Keep rule added",
                    "Added keep rule for $className. Re-run wizard to apply.",
                    com.intellij.notification.NotificationType.INFORMATION,
                )
                .notify(project)
        }
    }

    private fun extractClassName(element: PsiElement): String {
        val file = element.containingFile ?: return "Unknown"
        val text = file.text
        val pkgMatch = Regex("""^package\s+([\w.]+)""", RegexOption.MULTILINE).find(text)
        val clsMatch = Regex("""(?:class|interface|object|enum\s+class)\s+(\w+)""").find(text)
        val pkg = pkgMatch?.groupValues?.get(1) ?: ""
        val cls = clsMatch?.groupValues?.get(1) ?: file.name.removeSuffix(".kt").removeSuffix(".java")
        return if (pkg.isNotEmpty()) "$pkg.$cls" else cls
    }
}
