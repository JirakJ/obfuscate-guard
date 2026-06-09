package dev.htmeta.obfuscateguard.deobfuscator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.*
import java.awt.BorderLayout
import java.awt.datatransfer.StringSelection
import java.io.File
import javax.swing.Action
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

    private val inputArea = JTextArea(12, 80).apply {
        lineWrap = true
        wrapStyleWord = true
        toolTipText = "Paste obfuscated stack trace here"
    }
    private val outputArea = JTextArea(12, 80).apply {
        lineWrap = true
        wrapStyleWord = true
        isEditable = false
    }
    private val mappingField = com.intellij.openapi.ui.TextFieldWithBrowseButton()

    init {
        title = "Deobfuscate Stack Trace — ObfuscateGuard"
        setOKButtonText("Deobfuscate")
        // Pre-fill with a mapping file found in the project, if any
        findMappingFileInProject()?.let { mappingField.text = it.absolutePath }
        init()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout())

        val descriptor = com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
            .createSingleFileNoJarsDescriptor()
            .withTitle("Select Mapping File")
            .withDescription("Choose the mapping.txt file produced by ProGuard/R8")
        mappingField.addBrowseFolderListener(
            com.intellij.openapi.ui.TextBrowseFolderListener(descriptor, project),
        )

        val top = panel {
            group("Mapping File") {
                row {
                    cell(mappingField).align(Align.FILL).resizableColumn()
                        .comment("Auto-detected from the project when possible")
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
                row {
                    button("Copy Result") {
                        CopyPasteManager.getInstance().setContents(StringSelection(outputArea.text))
                    }
                }
            }
        }

        mainPanel.add(top, BorderLayout.CENTER)
        return mainPanel
    }

    // Keep the dialog open after "Deobfuscate" so users can iterate
    override fun doOKAction() {
        val mappingPath = mappingField.text.trim()
        if (mappingPath.isBlank()) {
            outputArea.text = "Please select a mapping file first."
            return
        }

        val mappingContent = try {
            File(mappingPath).readText()
        } catch (e: Exception) {
            outputArea.text = "Cannot read mapping file: ${e.message}"
            return
        }

        val input = inputArea.text
        if (input.isBlank()) {
            outputArea.text = "Please paste a stack trace."
            return
        }

        outputArea.text = StackTraceDeobfuscator.deobfuscate(input, mappingContent)
    }

    override fun createActions(): Array<Action> = arrayOf(okAction, cancelAction)

    private fun findMappingFileInProject(): File? {
        val basePath = project.basePath ?: return null
        val candidates = listOf(
            "app/build/outputs/mapping/release/mapping.txt",
            "build/outputs/mapping/release/mapping.txt",
            "app/build/outputs/mapping/debug/mapping.txt",
            "build/outputs/mapping/debug/mapping.txt",
            "mapping.txt",
        )
        return candidates.map { File(basePath, it) }.firstOrNull { it.isFile }
    }
}
