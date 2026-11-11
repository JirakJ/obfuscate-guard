package dev.htmeta.obfuscateguard.filetype

/**
 * Registry of ProGuard/R8 configuration directives with documentation.
 * Backs code completion, quick documentation, and config linting.
 */
object ProGuardDirectives {

    data class Directive(
        val name: String,
        val arguments: String,
        val description: String,
        /** True for directives that commonly hide real problems or remove protection. */
        val dangerous: Boolean = false,
        val dangerReason: String = "",
    )

    val ALL: List<Directive> = listOf(
        // Input/Output
        Directive("-include", "filename", "Recursively read configuration options from the given file."),
        Directive("-basedirectory", "directoryname", "Base directory for relative file names in the configuration."),
        Directive("-injars", "class_path", "Input jars (or apks, aabs, aars, wars, ears, jmods, zips, directories) to be processed."),
        Directive("-outjars", "class_path", "Output jars (or directories) for the processed code."),
        Directive("-libraryjars", "class_path", "Library jars required by the input code (not processed, not copied to output)."),
        Directive("-skipnonpubliclibraryclasses", "", "Skip non-public library classes while reading library jars."),
        Directive("-dontskipnonpubliclibraryclasses", "", "Do not skip non-public library classes (default since ProGuard 4.5)."),
        Directive("-dontskipnonpubliclibraryclassmembers", "", "Do not skip package-visible library class members."),
        Directive("-keepdirectories", "[directory_filter]", "Keep the specified directory entries in the output jars."),
        Directive("-target", "version", "Set the Java bytecode target version (e.g. 1.8, 11). Deprecated in newer ProGuard."),
        Directive("-forceprocessing", "", "Process the input even if the output is up to date."),

        // Keep rules
        Directive(
            "-keep",
            "[,modifier,...] class_specification",
            "Keep classes and class members from being removed or renamed. The most important directive.",
        ),
        Directive(
            "-keepclassmembers",
            "[,modifier,...] class_specification",
            "Keep class members from being removed or renamed, if their classes are kept.",
        ),
        Directive(
            "-keepclasseswithmembers",
            "[,modifier,...] class_specification",
            "Keep classes and their members, on condition that all specified members are present.",
        ),
        Directive(
            "-keepnames",
            "class_specification",
            "Keep names of classes and members if they are not removed by shrinking (= -keep,allowshrinking).",
        ),
        Directive("-keepclassmembernames", "class_specification", "Keep names of class members if they are not removed by shrinking."),
        Directive("-keepclasseswithmembernames", "class_specification", "Keep names of classes with all specified members present."),
        Directive("-if", "class_specification", "Conditional -keep: apply the following keep rule only if the specified classes exist."),
        Directive("-printseeds", "[filename]", "Print classes and members matched by keep rules (the 'seeds')."),

        // Shrinking
        Directive("-dontshrink", "", "Disable shrinking — unused classes and members are not removed."),
        Directive("-printusage", "[filename]", "Print dead code that was removed by shrinking."),
        Directive("-whyareyoukeeping", "class_specification", "Explain why the given classes and members are kept in the output."),

        // Optimization
        Directive("-dontoptimize", "", "Disable bytecode optimization."),
        Directive(
            "-optimizations",
            "optimization_filter",
            "Enable or disable specific optimizations (e.g. !code/simplification/arithmetic).",
        ),
        Directive("-optimizationpasses", "n", "Number of optimization passes (default 1)."),
        Directive(
            "-assumenosideeffects",
            "class_specification",
            "Assume the specified methods have no side effects — calls may be removed (use with care, typically for logging).",
            dangerous = true,
            dangerReason = "Removes method calls entirely; wrong specifications silently break behavior.",
        ),
        Directive("-assumevalues", "class_specification", "Assume fixed values or ranges for fields and methods."),
        Directive("-allowaccessmodification", "", "Allow widening access modifiers during optimization for better results."),
        Directive("-mergeinterfacesaggressively", "", "Allow merging interfaces, even if classes don't implement them."),

        // Obfuscation
        Directive(
            "-dontobfuscate",
            "",
            "Disable obfuscation — names are kept readable.",
            dangerous = true,
            dangerReason = "Disables the core protection. Remove it before a production release.",
        ),
        Directive("-printmapping", "[filename]", "Write the old name -> new name mapping to a file. Required for crash deobfuscation."),
        Directive("-applymapping", "filename", "Reuse a mapping from a previous build for incremental obfuscation."),
        Directive("-obfuscationdictionary", "filename", "Use words from the given file as obfuscated field and method names."),
        Directive("-classobfuscationdictionary", "filename", "Use words from the given file as obfuscated class names."),
        Directive("-packageobfuscationdictionary", "filename", "Use words from the given file as obfuscated package names."),
        Directive("-overloadaggressively", "", "Allow multiple fields and methods to get the same names."),
        Directive("-useuniqueclassmembernames", "", "Assign the same obfuscated name to members with the same name."),
        Directive("-dontusemixedcaseclassnames", "", "Do not generate mixed-case class names (needed on case-insensitive filesystems)."),
        Directive("-keeppackagenames", "[package_filter]", "Keep the specified package names from being obfuscated."),
        Directive("-flattenpackagehierarchy", "[package_name]", "Repackage all packages into the given parent package."),
        Directive("-repackageclasses", "[package_name]", "Repackage all class files into the given single package."),
        Directive(
            "-keepattributes",
            "[attribute_filter]",
            "Keep the specified attributes (Signature, SourceFile, LineNumberTable, *Annotation*, ...).",
        ),
        Directive("-keepparameternames", "", "Keep parameter names and types of kept methods."),
        Directive("-renamesourcefileattribute", "[string]", "Set the SourceFile attribute to the given constant string."),
        Directive("-adaptclassstrings", "[class_filter]", "Adapt string constants that correspond to obfuscated class names."),
        Directive(
            "-adaptresourcefilenames",
            "[file_filter]",
            "Rename resource files based on the obfuscated names of corresponding classes.",
        ),
        Directive("-adaptresourcefilecontents", "[file_filter]", "Update contents of resource files with obfuscated class names."),

        // Preverification
        Directive("-dontpreverify", "", "Disable preverification of processed class files."),
        Directive("-microedition", "", "Target Java Micro Edition (preverification rules)."),
        Directive("-android", "", "Target the Android platform (tunes processing accordingly)."),

        // Warnings and notes
        Directive("-verbose", "", "Write more information during processing."),
        Directive("-dontnote", "[class_filter]", "Suppress notes about potential configuration issues."),
        Directive(
            "-dontwarn",
            "[class_filter]",
            "Suppress warnings about unresolved references.",
            dangerous = true,
            dangerReason = "Broad filters (e.g. -dontwarn **) can hide real missing-class problems that crash at runtime.",
        ),
        Directive(
            "-ignorewarnings",
            "",
            "Continue processing despite warnings.",
            dangerous = true,
            dangerReason = "Warnings usually indicate classes that will be missing at runtime.",
        ),

        // Output diagnostics
        Directive("-printconfiguration", "[filename]", "Print the whole resolved configuration."),
        Directive("-dump", "[filename]", "Dump the internal structure of the processed class files."),
        Directive("-addconfigurationdebugging", "", "Instrument the code with debugging statements for missing keep rules (R8)."),
    )

    private val BY_NAME: Map<String, Directive> = ALL.associateBy { it.name }

    fun find(name: String): Directive? = BY_NAME[name]

    fun isKnown(name: String): Boolean = name in BY_NAME

    fun names(): List<String> = ALL.map { it.name }

    /** Closest known directive for a typo, or null when nothing is reasonably close. */
    fun closestMatch(name: String, maxDistance: Int = 3): String? =
        ALL.map { it.name }
            .map { it to levenshtein(name, it) }
            .filter { it.second <= maxDistance }
            .minByOrNull { it.second }
            ?.first

    internal fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        val dp = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..b.length) {
                val tmp = dp[j]
                dp[j] = minOf(
                    dp[j] + 1,
                    dp[j - 1] + 1,
                    prev + if (a[i - 1] == b[j - 1]) 0 else 1,
                )
                prev = tmp
            }
        }
        return dp[b.length]
    }
}
