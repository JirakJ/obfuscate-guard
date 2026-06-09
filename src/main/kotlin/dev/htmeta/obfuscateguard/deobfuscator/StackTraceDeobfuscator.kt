package dev.htmeta.obfuscateguard.deobfuscator

import dev.htmeta.obfuscateguard.core.spi.MappingEntry
import dev.htmeta.obfuscateguard.core.spi.MappingEntryType
import dev.htmeta.obfuscateguard.engine.proguard.ProGuardAdapter

/**
 * Pure stack-trace deobfuscation logic (retrace).
 *
 * Translates obfuscated class and method names back to the originals using
 * a ProGuard/R8 mapping file. Kept free of IDE dependencies so it is fully
 * unit-testable.
 */
object StackTraceDeobfuscator {

    private val AT_PATTERN = Regex("""(\s*at\s+)([\w.$]+)\.([\w$<>]+)\(([^)]*)\)""")
    private val LINE_NUMBER = Regex(""":(\d+)""")

    fun deobfuscate(stackTrace: String, mappingContent: String): String {
        val entries = ProGuardAdapter().parseMapping(mappingContent)
        if (entries.isEmpty()) return "No mapping entries found in the file"
        return deobfuscate(stackTrace, entries)
    }

    fun deobfuscate(stackTrace: String, entries: List<MappingEntry>): String {
        // obfuscated class -> original class
        val classMap = mutableMapOf<String, String>()
        // "obfuscatedClass.obfuscatedMember" -> original member simple name
        val memberMap = mutableMapOf<String, String>()

        for (entry in entries) {
            when (entry.type) {
                MappingEntryType.CLASS -> classMap[entry.obfuscatedName] = entry.originalName
                MappingEntryType.METHOD -> {
                    // originalName: "com.example.Foo.void run(java.lang.String)" —
                    // the method name is the token right before '(' (argument types may contain dots)
                    val beforeParen = entry.originalName.substringBefore("(")
                    val methodName = beforeParen.substringAfterLast(" ").substringAfterLast(".")
                    memberMap[entry.obfuscatedName] = methodName
                }
                MappingEntryType.FIELD -> {
                    // originalName: "com.example.Foo.int count" — name is the last token
                    val fieldName = entry.originalName.substringAfterLast(" ").substringAfterLast(".")
                    memberMap[entry.obfuscatedName] = fieldName
                }
            }
        }

        return stackTrace.lines().joinToString("\n") { line ->
            deobfuscateLine(line, classMap, memberMap)
        }
    }

    private fun deobfuscateLine(
        line: String,
        classMap: Map<String, String>,
        memberMap: Map<String, String>,
    ): String {
        val match = AT_PATTERN.find(line) ?: return resolveClassNames(line, classMap)

        val prefix = match.groupValues[1]
        val className = match.groupValues[2]
        val methodName = match.groupValues[3]
        val location = match.groupValues[4]

        val originalClass = classMap[className] ?: className
        val originalMethod = memberMap["$className.$methodName"] ?: methodName

        val sourceFile = originalClass.substringAfterLast(".").substringBefore("$") + ".java"
        val lineNum = LINE_NUMBER.find(location)?.groupValues?.get(1)
        val newLocation = if (lineNum != null) "$sourceFile:$lineNum" else sourceFile

        val rest = line.substring(match.range.last + 1)
        return "$prefix$originalClass.$originalMethod($newLocation)$rest"
    }

    /**
     * Replaces obfuscated class names appearing outside "at ..." frames
     * (e.g. in the exception message line). Matches whole identifiers only,
     * so short obfuscated names like "a.b" cannot corrupt unrelated text.
     */
    private fun resolveClassNames(line: String, classMap: Map<String, String>): String {
        var result = line
        for ((obf, orig) in classMap) {
            val pattern = Regex("""(?<![\w.$])${Regex.escape(obf)}(?![\w$])""")
            result = pattern.replace(result, Regex.escapeReplacement(orig))
        }
        return result
    }
}
