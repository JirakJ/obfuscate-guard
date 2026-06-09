package dev.htmeta.obfuscateguard.screenshots

import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.htmeta.obfuscateguard.ui.toolwindow.AnalysisPanel
import dev.htmeta.obfuscateguard.ui.toolwindow.OverviewPanel
import java.awt.Component
import java.awt.Container
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JComponent

/**
 * Renders real plugin UI components to PNG files for the README and the
 * JetBrains Marketplace listing. Disabled by default; opt in with:
 *
 *   ./gradlew test --tests "*ScreenshotGeneratorTest*" -PscreenshotsDir=$(pwd)/docs/screenshots
 */
class ScreenshotGeneratorTest : BasePlatformTestCase() {

    private val outputDir: File?
        get() = System.getProperty("obfuscateguard.screenshots.dir")
            ?.takeIf { it.isNotBlank() }
            ?.let { File(it) }

    fun testRenderScreenshots() {
        val dir = outputDir ?: return // opt-in only
        dir.mkdirs()

        renderProEditor(dir)
        renderToolWindowOverview(dir)
        renderToolWindowAnalysis(dir)

        val written = dir.listFiles { f -> f.extension == "png" }?.toList().orEmpty()
        assertTrue("No screenshots were produced", written.isNotEmpty())
        for (file in written) {
            assertTrue("Screenshot ${file.name} looks empty", file.length() > 5_000)
        }
    }

    private fun renderProEditor(dir: File) {
        myFixture.configureByText(
            "proguard-rules.pro",
            """
            # ProGuard configuration — edited with ObfuscateGuard
            -keepattributes SourceFile,LineNumberTable
            -printmapping build/outputs/mapping.txt
            -optimizationpasses 2

            # Keep the public API of the SDK
            -keep public class com.example.sdk.** { public protected *; }

            # Gson models — field names are part of the wire format
            -keepclassmembers class com.example.model.** {
                @com.google.gson.annotations.SerializedName <fields>;
            }

            -dontwarn okhttp3.**
            -dontobfuscate
            """.trimIndent(),
        )
        val editor = myFixture.editor as EditorEx
        editor.settings.isLineNumbersShown = true
        render(editor.component, 980, 460, File(dir, "pro-editor-linting.png"))
    }

    private fun renderToolWindowOverview(dir: File) {
        render(OverviewPanel(project), 980, 320, File(dir, "toolwindow-overview.png"))
    }

    private fun renderToolWindowAnalysis(dir: File) {
        val psiFile = myFixture.addFileToProject(
            "src/com/example/PluginLoader.java",
            """
            package com.example;
            import java.lang.reflect.Method;

            public class PluginLoader {
                public Object load(String name) throws Exception {
                    Class<?> clazz = Class.forName(name);
                    Method m = clazz.getDeclaredMethod("init");
                    return m.invoke(clazz.getDeclaredConstructor().newInstance());
                }
                public native void registerNative(long handle);
            }
            """.trimIndent(),
        )
        // The plugin's extension points are not loaded in the light fixture,
        // so run the built-in analyzer rules directly.
        val file = psiFile.virtualFile
        val content = String(file.contentsToByteArray())
        val rules = listOf(
            dev.htmeta.obfuscateguard.analyzer.rules.ReflectionUsageRule(),
            dev.htmeta.obfuscateguard.analyzer.rules.JniRule(),
            dev.htmeta.obfuscateguard.analyzer.rules.DynamicLoadingRule(),
        )
        val risks = rules.flatMap { it.analyze(project, file, content) }
        assertTrue("Sample analysis should find risks", risks.isNotEmpty())

        val panel = AnalysisPanel(project)
        panel.updateTable(risks)
        render(panel, 980, 420, File(dir, "toolwindow-analysis.png"))
    }

    private fun render(component: JComponent, width: Int, height: Int, target: File) {
        component.setBounds(0, 0, width, height)
        layoutRecursively(component)

        // 2x for crisp marketplace images
        val image = BufferedImage(width * 2, height * 2, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()
        g.scale(2.0, 2.0)
        g.color = component.background ?: java.awt.Color.WHITE
        g.fillRect(0, 0, width, height)
        component.print(g)
        g.dispose()

        ImageIO.write(image, "png", target)
    }

    private fun layoutRecursively(component: Component) {
        component.doLayout()
        if (component is Container) {
            for (child in component.components) {
                layoutRecursively(child)
            }
        }
    }
}
