package dev.htmeta.obfuscateguard.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/**
 * Gutter icon provider that marks lines containing obfuscation risks
 * with warning/error icons in the editor margin.
 */
class ObfuscationRiskLineMarkerProvider : LineMarkerProvider {

    private data class RiskMarker(
        val regex: Regex,
        val tooltip: String,
        val critical: Boolean,
    )

    private val markers = listOf(
        RiskMarker(
            Regex("""Class\.forName\s*\("""),
            "ObfuscateGuard: Class.forName() — class name will change after obfuscation",
            true,
        ),
        RiskMarker(
            Regex("""\.(getDeclaredMethod|getDeclaredField|getDeclaredConstructor)\s*\("""),
            "ObfuscateGuard: Reflective member access — name will change after obfuscation",
            true,
        ),
        RiskMarker(
            Regex("""\bnative\s+\w+\s+\w+\s*\("""),
            "ObfuscateGuard: Native method — JNI binding requires exact name",
            true,
        ),
        RiskMarker(
            Regex("""@SerializedName|@JsonProperty|@SerialName|@JsonName"""),
            "ObfuscateGuard: Serialization annotation — verify keep rules",
            false,
        ),
        RiskMarker(
            Regex("""@(Component|Service|Repository|Controller|Inject|Named|HiltAndroidApp|AndroidEntryPoint)"""),
            "ObfuscateGuard: DI annotation — class discovered by name at runtime",
            false,
        ),
        RiskMarker(
            Regex("""ServiceLoader\.load\s*\("""),
            "ObfuscateGuard: ServiceLoader uses class names in META-INF/services",
            true,
        ),
        RiskMarker(
            Regex("""@Entity|@Table"""),
            "ObfuscateGuard: JPA entity — class/field names used by ORM",
            true,
        ),
    )

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(
        elements: MutableList<out PsiElement>,
        result: MutableCollection<in LineMarkerInfo<*>>,
    ) {
        if (elements.isEmpty()) return
        val file = elements.firstOrNull()?.containingFile ?: return
        val ext = file.virtualFile?.extension ?: return
        if (ext !in setOf("java", "kt", "kts")) return

        val settings = dev.htmeta.obfuscateguard.ui.settings.ObfuscateGuardSettings.getInstance()
        if (!settings.enabled || !settings.showGutterIcons) return

        val text = file.text
        val lines = text.lines()
        val processedLines = mutableSetOf<Int>()

        for ((lineIndex, line) in lines.withIndex()) {
            if (lineIndex in processedLines) continue

            for (marker in markers) {
                if (marker.regex.containsMatchIn(line)) {
                    val offset = lineOffset(text, lineIndex)
                    val element = file.findElementAt(offset) ?: continue

                    if (element in elements) {
                        val icon = if (marker.critical) AllIcons.General.Warning else AllIcons.General.Information
                        val tooltipText = marker.tooltip
                        result.add(
                            LineMarkerInfo(
                                element,
                                element.textRange,
                                icon,
                                { tooltipText },
                                null,
                                GutterIconRenderer.Alignment.LEFT,
                                { tooltipText },
                            )
                        )
                        processedLines.add(lineIndex)
                        break
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
