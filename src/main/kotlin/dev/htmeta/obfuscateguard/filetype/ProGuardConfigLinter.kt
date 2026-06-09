package dev.htmeta.obfuscateguard.filetype

/**
 * Pure linting logic for ProGuard configuration content.
 * Used by [ProGuardLintAnnotator] in the editor; fully unit-testable.
 */
object ProGuardConfigLinter {

    enum class IssueSeverity { ERROR, WARNING, WEAK_WARNING }

    data class LintIssue(
        /** Offset of the issue start in the whole file content. */
        val startOffset: Int,
        val endOffset: Int,
        val lineNumber: Int,
        val severity: IssueSeverity,
        val message: String,
    )

    private val DIRECTIVE_AT_LINE_START = Regex("""^(-[A-Za-z]+)""")

    fun lint(content: String): List<LintIssue> {
        val issues = mutableListOf<LintIssue>()
        var offset = 0
        val seenSingletonDirectives = mutableMapOf<String, Int>()

        content.lines().forEachIndexed { index, line ->
            val trimmed = line.trimStart()
            val indent = line.length - trimmed.length
            val match = DIRECTIVE_AT_LINE_START.find(trimmed)

            if (match != null && indent == 0) {
                val name = match.groupValues[1]
                val start = offset + indent
                val end = start + name.length

                val directive = ProGuardDirectives.find(name)
                when {
                    directive == null -> {
                        val suggestion = ProGuardDirectives.closestMatch(name)
                        val hint = if (suggestion != null) " Did you mean '$suggestion'?" else ""
                        issues.add(
                            LintIssue(start, end, index + 1, IssueSeverity.ERROR, "Unknown directive '$name'.$hint")
                        )
                    }
                    directive.dangerous -> {
                        val argument = trimmed.removePrefix(name).trim()
                        val isBroadDontwarn = name == "-dontwarn" && (argument.isEmpty() || argument == "**" || argument == "*")
                        val severity = if (name == "-dontwarn" && !isBroadDontwarn) {
                            // Targeted -dontwarn for a specific package is normal practice
                            null
                        } else {
                            IssueSeverity.WARNING
                        }
                        if (severity != null) {
                            issues.add(
                                LintIssue(start, end, index + 1, severity, "${directive.dangerReason} (${directive.name})")
                            )
                        }
                    }
                    else -> {
                        // Flag duplicates of directives that only make sense once
                        if (name in SINGLETON_DIRECTIVES) {
                            val firstLine = seenSingletonDirectives[name]
                            if (firstLine != null) {
                                issues.add(
                                    LintIssue(
                                        start, end, index + 1, IssueSeverity.WEAK_WARNING,
                                        "Duplicate directive '$name' (already specified on line $firstLine)."
                                    )
                                )
                            } else {
                                seenSingletonDirectives[name] = index + 1
                            }
                        }
                    }
                }
            }

            offset += line.length + 1
        }

        return issues
    }

    /** Checks useful for release readiness, not tied to a specific line. */
    fun releaseChecks(content: String): List<String> {
        val findings = mutableListOf<String>()
        val directives = content.lines()
            .map { it.trim() }
            .filter { it.startsWith("-") }

        if (directives.none { it.startsWith("-printmapping") }) {
            findings.add("No -printmapping: without a mapping file you cannot deobfuscate production crashes.")
        }
        if (directives.any { it == "-dontobfuscate" }) {
            findings.add("-dontobfuscate is present: obfuscation is disabled entirely.")
        }
        if (directives.none { it.startsWith("-keepattributes") && it.contains("LineNumberTable") }) {
            findings.add("LineNumberTable attribute is not kept: stack traces will have no line numbers.")
        }
        return findings
    }

    private val SINGLETON_DIRECTIVES = setOf(
        "-printmapping", "-applymapping", "-repackageclasses", "-flattenpackagehierarchy",
        "-optimizationpasses", "-target", "-printseeds", "-printusage", "-printconfiguration",
    )
}
