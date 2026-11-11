package dev.htmeta.obfuscateguard.core

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import dev.htmeta.obfuscateguard.core.model.*
import dev.htmeta.obfuscateguard.core.spi.ObfuscationEngineAdapter

/**
 * Detects project type, build system, modules, and existing obfuscation configuration.
 */
class ProjectDetector {

    fun detect(project: Project): ProjectProfile {
        val baseDir = project.guessProjectDir() ?: return ProjectProfile()

        val buildSystem = detectBuildSystem(baseDir)
        val projectType = detectProjectType(baseDir, buildSystem)
        val modules = detectModules(baseDir, buildSystem)
        val detectedEngines = detectEngines(project)

        return ProjectProfile(
            projectType = projectType,
            buildSystem = buildSystem,
            modules = modules,
            detectedEngines = detectedEngines,
            projectName = project.name,
            basePath = baseDir.path,
        )
    }

    private fun detectBuildSystem(baseDir: com.intellij.openapi.vfs.VirtualFile): BuildSystem = when {
        baseDir.findChild("build.gradle.kts") != null -> BuildSystem.GRADLE
        baseDir.findChild("build.gradle") != null -> BuildSystem.GRADLE
        baseDir.findChild("pom.xml") != null -> BuildSystem.MAVEN
        else -> BuildSystem.UNKNOWN
    }

    private fun detectProjectType(
        baseDir: com.intellij.openapi.vfs.VirtualFile,
        buildSystem: BuildSystem,
    ): ProjectType {
        return when (buildSystem) {
            BuildSystem.GRADLE -> {
                val buildFile = baseDir.findChild("build.gradle.kts") ?: baseDir.findChild("build.gradle")
                gradleProjectType(buildFile?.let { readFileSafe(it) }.orEmpty())
            }
            BuildSystem.MAVEN -> mavenProjectType(baseDir.findChild("pom.xml")?.let { readFileSafe(it) }.orEmpty())
            else -> ProjectType.UNKNOWN
        }
    }

    private fun detectModules(
        baseDir: com.intellij.openapi.vfs.VirtualFile,
        buildSystem: BuildSystem,
    ): List<ModuleInfo> {
        if (buildSystem == BuildSystem.GRADLE) {
            val settingsFile = baseDir.findChild("settings.gradle.kts") ?: baseDir.findChild("settings.gradle")
            val content = settingsFile?.let { readFileSafe(it) } ?: return emptyList()
            val includePattern = Regex("""include\s*\(\s*["']([^"']+)["']""")
            return includePattern.findAll(content).map { match ->
                val moduleName = match.groupValues[1]
                val modulePath = moduleName.replace(":", "/")
                val moduleDir = baseDir.findFileByRelativePath(modulePath)
                ModuleInfo(
                    name = moduleName,
                    path = modulePath,
                    hasExistingConfig = moduleDir?.findChild("proguard-rules.pro") != null,
                )
            }.toList()
        }
        return emptyList()
    }

    private fun detectEngines(project: Project): List<EngineType> =
        ObfuscationEngineAdapter.allAdapters()
            .filter { it.detectInstallation(project) != null }
            .map { it.engineType }

    private fun readFileSafe(file: com.intellij.openapi.vfs.VirtualFile): String? = try {
        String(file.contentsToByteArray())
    } catch (e: Exception) {
        LOG.debug("Cannot read file: ${file.path}", e)
        null
    }

    companion object {
        private val LOG = Logger.getInstance(ProjectDetector::class.java)

        /** Project type from a Gradle build script. */
        fun gradleProjectType(content: String): ProjectType = when {
            content.contains("com.android.application") -> ProjectType.ANDROID_APP
            content.contains("com.android.library") -> ProjectType.ANDROID_LIBRARY
            content.contains("org.jetbrains.intellij") -> ProjectType.PLUGIN
            content.contains("application") && content.contains("mainClass") -> ProjectType.APP
            content.contains("java-library") || content.contains("kotlin(\"jvm\")") -> ProjectType.LIBRARY
            else -> ProjectType.APP
        }

        /** Project type from a Maven POM (`pom` packaging = aggregator/library, otherwise an application). */
        fun mavenProjectType(content: String): ProjectType =
            if (content.contains("<packaging>pom</packaging>")) ProjectType.LIBRARY else ProjectType.APP
    }
}
