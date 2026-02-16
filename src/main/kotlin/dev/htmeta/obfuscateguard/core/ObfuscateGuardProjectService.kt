package dev.htmeta.obfuscateguard.core

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import dev.htmeta.obfuscateguard.core.model.*
import dev.htmeta.obfuscateguard.core.spi.AnalyzerRule
import dev.htmeta.obfuscateguard.core.spi.ObfuscationEngineAdapter

/**
 * Central project-level service for ObfuscateGuard.
 * Provides project detection, analysis orchestration, and run management.
 */
@Service(Service.Level.PROJECT)
class ObfuscateGuardProjectService(private val project: Project) {

    private var cachedProfile: ProjectProfile? = null
    private var cachedConfig: ObfuscationConfig? = null
    private var lastAnalysisResults: List<RiskItem> = emptyList()

    val profile: ProjectProfile?
        get() = cachedProfile

    val config: ObfuscationConfig?
        get() = cachedConfig

    val analysisResults: List<RiskItem>
        get() = lastAnalysisResults

    fun detectProject(): ProjectProfile {
        val detector = ProjectDetector()
        val profile = detector.detect(project)
        cachedProfile = profile
        LOG.info("Detected project: ${profile.projectType} / ${profile.buildSystem}")
        return profile
    }

    fun updateConfig(config: ObfuscationConfig) {
        cachedConfig = config
        LOG.info("Updated obfuscation config: engine=${config.engine}, level=${config.level}")
    }

    fun analyzeRisks(): List<RiskItem> {
        val rules = AnalyzerRule.allRules()
        val results = mutableListOf<RiskItem>()

        collectSourceFiles(project).forEach { file ->
            val content = try {
                String(file.contentsToByteArray())
            } catch (e: Exception) {
                LOG.debug("Cannot read file: ${file.path}", e)
                return@forEach
            }

            rules.forEach { rule ->
                try {
                    results.addAll(rule.analyze(project, file, content))
                } catch (e: Exception) {
                    LOG.warn("Analyzer rule ${rule.id} failed on ${file.path}", e)
                }
            }
        }

        lastAnalysisResults = results
        LOG.info("Analysis complete: ${results.size} risks found")
        return results
    }

    fun generateConfig(): EngineSpecificConfig? {
        val cfg = cachedConfig ?: return null
        val prof = cachedProfile ?: detectProject()
        val adapter = ObfuscationEngineAdapter.findAdapter(cfg.engine) ?: return null
        return adapter.generateConfig(prof, cfg)
    }

    private fun collectSourceFiles(project: Project): List<VirtualFile> {
        val files = mutableListOf<VirtualFile>()
        val roots = ProjectRootManager.getInstance(project).contentSourceRoots
        if (roots.isEmpty()) {
            val baseDir = project.basePath?.let {
                com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByPath(it)
            } ?: return emptyList()
            collectFilesRecursive(baseDir, files)
        } else {
            roots.forEach { root -> collectFilesRecursive(root, files) }
        }
        return files
    }

    private fun collectFilesRecursive(dir: VirtualFile, acc: MutableList<VirtualFile>) {
        for (child in dir.children) {
            if (child.isDirectory) {
                if (child.name !in SKIP_DIRS) {
                    collectFilesRecursive(child, acc)
                }
            } else if (child.extension in SOURCE_EXTENSIONS) {
                acc.add(child)
            }
        }
    }

    companion object {
        private val LOG = Logger.getInstance(ObfuscateGuardProjectService::class.java)
        private val SOURCE_EXTENSIONS = setOf("java", "kt", "kts")
        private val SKIP_DIRS = setOf("build", ".gradle", ".idea", "node_modules", ".git", "out", "target")

        fun getInstance(project: Project): ObfuscateGuardProjectService =
            project.getService(ObfuscateGuardProjectService::class.java)
    }
}
