package dev.htmeta.obfuscateguard.core.spi

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import dev.htmeta.obfuscateguard.core.model.RiskCategory
import dev.htmeta.obfuscateguard.core.model.RiskItem

/**
 * SPI for analyzer rules that detect obfuscation risks in source code.
 * Registered via `dev.htmeta.obfuscateguard.analyzerRule` extension point.
 */
interface AnalyzerRule {

    val id: String
    val category: RiskCategory
    val displayName: String

    /**
     * Analyze a single file for obfuscation risks.
     * Runs on a background thread; implementations must not access EDT.
     */
    fun analyze(project: Project, file: VirtualFile, content: String): List<RiskItem>

    companion object {
        val EP_NAME = ExtensionPointName.create<AnalyzerRule>(
            "dev.htmeta.obfuscateguard.analyzerRule"
        )

        fun allRules(): List<AnalyzerRule> = EP_NAME.extensionList
    }
}
