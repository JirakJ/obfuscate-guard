package dev.htmeta.obfuscateguard.analyzer.rules

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import dev.htmeta.obfuscateguard.core.model.*
import dev.htmeta.obfuscateguard.core.spi.AnalyzerRule
import java.util.UUID

/**
 * Detects reflection usage patterns that will break after obfuscation.
 * Patterns: Class.forName(), Method.invoke(), Field.get(), getDeclaredMethod(), etc.
 */
class ReflectionUsageRule : AnalyzerRule {
    override val id = "reflection-usage"
    override val category = RiskCategory.REFLECTION
    override val displayName = "Reflection Usage"

    private val patterns = listOf(
        PatternDef("Class\\.forName\\s*\\(", "Class.forName() — class name will change after obfuscation", RiskSeverity.CRITICAL),
        PatternDef("\\.getDeclaredMethod\\s*\\(", "getDeclaredMethod() — method name will change", RiskSeverity.CRITICAL),
        PatternDef("\\.getDeclaredField\\s*\\(", "getDeclaredField() — field name will change", RiskSeverity.CRITICAL),
        PatternDef("\\.getMethod\\s*\\(", "getMethod() — method name will change", RiskSeverity.WARNING),
        PatternDef("\\.getField\\s*\\(", "getField() — field name will change", RiskSeverity.WARNING),
        PatternDef("Method\\.invoke\\s*\\(", "Method.invoke() — reflective invocation", RiskSeverity.WARNING),
        PatternDef("Field\\.get\\s*\\(|Field\\.set\\s*\\(", "Field access via reflection", RiskSeverity.WARNING),
    )

    override fun analyze(project: Project, file: VirtualFile, content: String): List<RiskItem> =
        analyzeWithPatterns(file, content, patterns, category)
}

class SerializationRule : AnalyzerRule {
    override val id = "serialization-patterns"
    override val category = RiskCategory.SERIALIZATION
    override val displayName = "Serialization Patterns"

    private val patterns = listOf(
        PatternDef("@SerializedName", "@SerializedName — Gson field name mapping", RiskSeverity.CRITICAL),
        PatternDef("@JsonProperty", "@JsonProperty — Jackson field name mapping", RiskSeverity.CRITICAL),
        PatternDef("@XmlElement|@XmlAttribute", "XML serialization annotation", RiskSeverity.CRITICAL),
        PatternDef("@JsonName|@SerialName", "Kotlin serialization name mapping", RiskSeverity.CRITICAL),
        PatternDef("implements\\s+Serializable|:\\s*Serializable", "Serializable interface — field names matter", RiskSeverity.WARNING),
        PatternDef("implements\\s+Parcelable|:\\s*Parcelable", "Parcelable — field names and CREATOR matter", RiskSeverity.WARNING),
        PatternDef("@Entity|@Table|@Column", "JPA/Room entity — class/field names used by ORM", RiskSeverity.CRITICAL),
    )

    override fun analyze(project: Project, file: VirtualFile, content: String): List<RiskItem> =
        analyzeWithPatterns(file, content, patterns, category)
}

class JniRule : AnalyzerRule {
    override val id = "jni-usage"
    override val category = RiskCategory.JNI
    override val displayName = "JNI / Native Methods"

    private val patterns = listOf(
        PatternDef("\\bnative\\s+\\w+\\s+\\w+\\s*\\(", "Native method — JNI binding uses exact method name", RiskSeverity.CRITICAL),
        PatternDef("System\\.loadLibrary\\s*\\(", "System.loadLibrary() — native library loading", RiskSeverity.WARNING),
        PatternDef("System\\.load\\s*\\(", "System.load() — native library loading", RiskSeverity.WARNING),
    )

    override fun analyze(project: Project, file: VirtualFile, content: String): List<RiskItem> =
        analyzeWithPatterns(file, content, patterns, category)
}

class DiFrameworkRule : AnalyzerRule {
    override val id = "di-framework"
    override val category = RiskCategory.DI_FRAMEWORK
    override val displayName = "DI Framework Usage"

    private val patterns = listOf(
        PatternDef("@Component|@Service|@Repository|@Controller", "Spring component — discovered by class name", RiskSeverity.CRITICAL),
        PatternDef("@Autowired|@Value\\(", "Spring injection — field/constructor name matters", RiskSeverity.WARNING),
        PatternDef("@Inject|@Named", "Dagger/Hilt/Guice injection", RiskSeverity.CRITICAL),
        PatternDef("@Module|@Provides|@Binds", "Dagger module — method names used for binding", RiskSeverity.WARNING),
        PatternDef("@HiltAndroidApp|@AndroidEntryPoint", "Hilt entry point — class name matters", RiskSeverity.CRITICAL),
        PatternDef("@Bean|@Configuration", "Spring configuration — method names are bean IDs", RiskSeverity.WARNING),
    )

    override fun analyze(project: Project, file: VirtualFile, content: String): List<RiskItem> =
        analyzeWithPatterns(file, content, patterns, category)
}

class DynamicLoadingRule : AnalyzerRule {
    override val id = "dynamic-loading"
    override val category = RiskCategory.DYNAMIC_LOADING
    override val displayName = "Dynamic Class Loading"

    private val patterns = listOf(
        PatternDef("ClassLoader\\.loadClass\\s*\\(", "ClassLoader.loadClass() — class name used at runtime", RiskSeverity.CRITICAL),
        PatternDef("ServiceLoader\\.load\\s*\\(", "ServiceLoader.load() — META-INF/services uses class names", RiskSeverity.CRITICAL),
        PatternDef("Thread\\.currentThread\\(\\)\\.contextClassLoader", "Context ClassLoader usage", RiskSeverity.INFO),
        PatternDef("URLClassLoader", "URLClassLoader — dynamic class loading", RiskSeverity.WARNING),
    )

    override fun analyze(project: Project, file: VirtualFile, content: String): List<RiskItem> =
        analyzeWithPatterns(file, content, patterns, category)
}

// -- Shared utilities --

internal data class PatternDef(val regex: String, val description: String, val severity: RiskSeverity)

internal fun analyzeWithPatterns(
    file: VirtualFile,
    content: String,
    patterns: List<PatternDef>,
    category: RiskCategory,
): List<RiskItem> {
    val results = mutableListOf<RiskItem>()
    val lines = content.lines()

    for (patternDef in patterns) {
        val regex = Regex(patternDef.regex)
        lines.forEachIndexed { index, line ->
            if (regex.containsMatchIn(line)) {
                val className = extractClassName(file, content) ?: file.nameWithoutExtension
                results.add(
                    RiskItem(
                        id = UUID.randomUUID().toString(),
                        filePath = file.path,
                        lineNumber = index + 1,
                        className = className,
                        category = category,
                        severity = patternDef.severity,
                        description = patternDef.description,
                        suggestedKeepRule = generateKeepRule(className, patternDef, category),
                    )
                )
            }
        }
    }
    return results
}

private fun extractClassName(file: VirtualFile, content: String): String? {
    val packagePattern = Regex("""^package\s+([\w.]+)""", RegexOption.MULTILINE)
    val classPattern = Regex("""(?:class|interface|object|enum\s+class)\s+(\w+)""")
    val pkg = packagePattern.find(content)?.groupValues?.get(1) ?: ""
    val cls = classPattern.find(content)?.groupValues?.get(1) ?: return null
    return if (pkg.isNotEmpty()) "$pkg.$cls" else cls
}

private fun generateKeepRule(className: String, pattern: PatternDef, category: RiskCategory): KeepRule {
    val rule = when {
        pattern.severity == RiskSeverity.CRITICAL -> "class $className { *; }"
        else -> "class $className { <methods>; }"
    }
    return KeepRule(
        pattern = rule,
        reason = "${category.displayName}: ${pattern.description}",
        source = category,
        needsReview = pattern.severity != RiskSeverity.CRITICAL,
    )
}
