package dev.htmeta.obfuscateguard.deobfuscator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.*
import dev.htmeta.obfuscateguard.core.spi.ObfuscationEngineAdapter
import dev.htmeta.obfuscateguard.core.spi.MappingEntryType
import dev.htmeta.obfuscateguard.core.model.EngineType
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea

/**
 * Stack trace deobfuscator — paste an obfuscated crash log and get the original.
 * This is the #1 must-have feature for any obfuscation tool.
 */
class DeobfuscateStackTraceAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        DeobfuscateDialog(project).show()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}

class DeobfuscateDialog(private val project: Project) : DialogWrapper(project) {

    private val inputArea = JTextArea(15, 80).apply {
        lineWrap = true
        wrapStyleWord = true
        toolTipText = "Paste obfuscated stack trace here"
    }
    private val outputArea = JTextArea(15, 80).apply {
        lineWrap = true
        wrapStyleWord = true
        isEditable = false
    }
    private var mappingContent: String = ""

    init {
        title = "Deobfuscate Stack Trace — ObfuscateGuard"
        setOKButtonText("Deobfuscate")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout())

        val top = panel {
            group("Mapping File") {
                row {
                    val descriptor = com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
                        .createSingleFileNoJarsDescriptor()
                        .withTitle("Select Mapping File")
                        .withDescription("Choose the mapping.txt file produced by ProGuard/R8")
                    val browseBtn = com.intellij.openapi.ui.TextFieldWithBrowseButton()
                    browseBtn.addBrowseFolderListener(
                        com.intellij.openapi.ui.TextBrowseFolderListener(descriptor, project),
                    )
                    browseBtn.addActionListener {
                        val path = browseBtn.text
                        if (path.isNotBlank()) {
                            mappingContent = try { java.io.File(path).readText() } catch (_: Exception) { "" }
                        }
                    }
                    cell(browseBtn).align(Align.FILL).resizableColumn()
                }
            }
            group("Obfuscated Stack Trace") {
                row {
                    scrollCell(inputArea).align(Align.FILL).resizableColumn()
                }.resizableRow()
            }
            group("Deobfuscated Result") {
                row {
                    scrollCell(outputArea).align(Align.FILL).resizableColumn()
                }.resizableRow()
            }
        }

        mainPanel.add(top, BorderLayout.CENTER)
        return mainPanel
    }

    override fun doOKAction() {
        if (mappingContent.isBlank()) {
            outputArea.text = "Please select a mapping file first."
            return
        }

        val input = inputArea.text
        if (input.isBlank()) {
            outputArea.text = "Please paste a stack trace."
            return
        }

        val deobfuscated = deobfuscate(input, mappingContent)
        outputArea.text = deobfuscated
    }

    companion object {
        fun deobfuscate(stackTrace: String, mappingContent: String): String {
            val adapter = ObfuscationEngineAdapter.findAdapter(EngineType.PROGUARD)
                ?: return "ProGuard adapter not found"

            val entries = adapter.parseMapping(mappingContent)
            if (entries.isEmpty()) return "No mapping entries found in the file"

            val classMap = mutableMapOf<String, String>()
            val memberMap = mutableMapOf<String, String>()

            for (entry in entries) {
                when (entry.type) {
                    MappingEntryType.CLASS -> classMap[entry.obfuscatedName] = entry.originalName
                    else -> memberMap[entry.obfuscatedName] = entry.originalName
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
            val atPattern = Regex("""(\s+at\s+)([\w.$]+)\.([\w$]+)\(([^)]*)\)""")
            val match = atPattern.find(line) ?: return resolveClassNames(line, classMap)

            val prefix = match.groupValues[1]
            val className = match.groupValues[2]
            val methodName = match.groupValues[3]
            val location = match.groupValues[4]

            val originalClass = classMap[className] ?: className
            val memberKey = "$className.$methodName"
            val originalMethod = memberMap[memberKey]?.substringAfterLast(".") ?: methodName

            val sourceFile = originalClass.substringAfterLast(".") + ".java"
            val lineNum = Regex(""":(\d+)""").find(location)?.groupValues?.get(1) ?: ""
            val newLocation = if (lineNum.isNotEmpty()) "$sourceFile:$lineNum" else sourceFile

            return "$prefix$originalClass.$originalMethod($newLocation)"
        }

        private fun resolveClassNames(line: String, classMap: Map<String, String>): String {
            var result = line
            for ((obf, orig) in classMap) {
                if (result.contains(obf)) {
                    result = result.replace(obf, orig)
                }
            }
            return result
        }
    }
}
