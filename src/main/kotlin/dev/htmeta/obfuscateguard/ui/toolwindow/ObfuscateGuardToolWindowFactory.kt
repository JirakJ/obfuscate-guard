package dev.htmeta.obfuscateguard.ui.toolwindow

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.table.JBTable
import dev.htmeta.obfuscateguard.core.ObfuscateGuardProjectService
import dev.htmeta.obfuscateguard.core.model.RiskItem
import dev.htmeta.obfuscateguard.core.model.RiskSeverity
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel

class ObfuscateGuardToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()

        // Overview tab
        val overviewPanel = OverviewPanel(project)
        val overviewContent = contentFactory.createContent(overviewPanel, "Overview", false)
        toolWindow.contentManager.addContent(overviewContent)

        // Analysis tab
        val analysisPanel = AnalysisPanel(project)
        val analysisContent = contentFactory.createContent(analysisPanel, "Analysis", false)
        toolWindow.contentManager.addContent(analysisContent)
    }
}

class OverviewPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val statusLabel = JBLabel("No obfuscation configuration detected.")

    init {
        val service = ObfuscateGuardProjectService.getInstance(project)

        val content = panel {
            group("ObfuscateGuard") {
                row {
                    cell(statusLabel)
                }
                row {
                    button("Configure Obfuscation...") {
                        dev.htmeta.obfuscateguard.ui.wizard.ObfuscationWizardDialog(project).show()
                        refreshStatus()
                    }
                    button("Analyze Risks") {
                        val results = service.analyzeRisks()
                        statusLabel.text = "Analysis: ${results.size} risks found"
                    }
                }
            }
            group("Quick Info") {
                row("Project:") { label(project.name) }
                row("Status:") {
                    val config = service.config
                    label(if (config != null) "Configured (${config.engine.displayName})" else "Not configured")
                }
            }
        }

        add(content, BorderLayout.NORTH)
    }

    private fun refreshStatus() {
        val config = ObfuscateGuardProjectService.getInstance(project).config
        statusLabel.text = if (config != null) {
            "Configured: ${config.engine.displayName} / ${config.level.displayName}"
        } else {
            "No obfuscation configuration detected."
        }
    }
}

class AnalysisPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val tableModel: DefaultTableModel
    private val table: JBTable
    private val countLabel = JBLabel("No analysis results yet.")

    init {
        val columns = arrayOf<Any>("Severity", "Category", "File", "Line", "Description")
        tableModel = object : DefaultTableModel(columns, 0) {
            override fun isCellEditable(row: Int, column: Int) = false
        }
        table = JBTable(tableModel).apply {
            autoCreateRowSorter = true
        }

        val toolbar = JPanel().apply {
            add(JButton("Run Analysis").apply {
                addActionListener { runAnalysis() }
            })
            add(JButton("Generate Keep Rules").apply {
                addActionListener { generateKeepRules() }
            })
            add(countLabel)
        }

        add(toolbar, BorderLayout.NORTH)
        add(JBScrollPane(table), BorderLayout.CENTER)
    }

    private fun runAnalysis() {
        val service = ObfuscateGuardProjectService.getInstance(project)
        val results = service.analyzeRisks()
        updateTable(results)
    }

    private fun updateTable(results: List<RiskItem>) {
        tableModel.rowCount = 0
        results.forEach { risk ->
            tableModel.addRow(
                arrayOf(
                    "${risk.severity.icon} ${risk.severity.displayName}",
                    risk.category.displayName,
                    risk.filePath.substringAfterLast("/"),
                    risk.lineNumber,
                    risk.description,
                )
            )
        }
        val critical = results.count { it.severity == RiskSeverity.CRITICAL }
        val warnings = results.count { it.severity == RiskSeverity.WARNING }
        countLabel.text = "${results.size} risks ($critical critical, $warnings warnings)"
    }

    private fun generateKeepRules() {
        val service = ObfuscateGuardProjectService.getInstance(project)
        val results = service.analysisResults
        val rules = results.mapNotNull { it.suggestedKeepRule }.distinctBy { it.pattern }

        if (rules.isEmpty()) {
            countLabel.text = "No risks found. Run analysis first."
            return
        }

        val config = service.config ?: return
        service.updateConfig(config.copy(keepRules = rules))
        countLabel.text = "Generated ${rules.size} keep rules. Re-run wizard to apply."
    }
}
