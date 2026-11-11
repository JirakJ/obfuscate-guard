package dev.htmeta.obfuscateguard.linemarker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import dev.htmeta.obfuscateguard.util.LineOffsets
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
        val offsets = LineOffsets(text)
        text.lines().forEachIndexed { lineIndex, line ->
            val marker = markers.firstOrNull { it.regex.containsMatchIn(line) } ?: return@forEachIndexed
            val element = file.findElementAt(offsets.firstNonBlank(lineIndex))
            if (element != null && element in elements) result.add(markerInfo(element, marker))
        }
    }

    private fun markerInfo(element: PsiElement, marker: RiskMarker): LineMarkerInfo<PsiElement> {
        val icon = if (marker.critical) AllIcons.General.Warning else AllIcons.General.Information
        val tooltipText = marker.tooltip
        return LineMarkerInfo(
            element,
            element.textRange,
            icon,
            { tooltipText },
            null,
            GutterIconRenderer.Alignment.LEFT,
            { tooltipText },
        )
    }
}
