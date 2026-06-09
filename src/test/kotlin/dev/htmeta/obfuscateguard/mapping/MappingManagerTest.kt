package dev.htmeta.obfuscateguard.mapping

import dev.htmeta.obfuscateguard.core.model.EngineType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class MappingManagerTest {

    private val mappingV1 = """
        com.example.Foo -> a.a:
            void bar() -> a
        com.example.Baz -> a.b:
            int qux -> b
    """.trimIndent()

    private val mappingV2 = """
        com.example.Foo -> b.a:
            void bar() -> c
        com.example.New -> b.b:
    """.trimIndent()

    @Test
    fun `storeMapping persists content and computes stats`(@TempDir tempDir: Path) {
        val manager = MappingManager(tempDir.toString())
        val record = manager.storeMapping(mappingV1, EngineType.PROGUARD, gitCommitHash = "abc123", buildVariant = "release")

        assertEquals(2, record.stats.classCount)
        assertEquals(1, record.stats.methodCount)
        assertEquals(1, record.stats.fieldCount)
        assertEquals("abc123", record.gitCommitHash)
        assertEquals(mappingV1, manager.getMapping(record.id))
    }

    @Test
    fun `records survive a manager restart`(@TempDir tempDir: Path) {
        val record = MappingManager(tempDir.toString()).storeMapping(mappingV1, EngineType.PROGUARD)

        val reloaded = MappingManager(tempDir.toString())
        assertEquals(1, reloaded.listRecords().size)
        assertEquals(record.id, reloaded.listRecords()[0].id)
        assertEquals(mappingV1, reloaded.getMapping(record.id))
    }

    @Test
    fun `deleteMapping removes record and file`(@TempDir tempDir: Path) {
        val manager = MappingManager(tempDir.toString())
        val record = manager.storeMapping(mappingV1, EngineType.PROGUARD)

        assertTrue(manager.deleteMapping(record.id))
        assertTrue(manager.listRecords().isEmpty())
        assertNull(manager.getMapping(record.id))
        // Deletion is persisted too
        assertTrue(MappingManager(tempDir.toString()).listRecords().isEmpty())
    }

    @Test
    fun `diffMappings reports added removed and changed entries`(@TempDir tempDir: Path) {
        val manager = MappingManager(tempDir.toString())
        val r1 = manager.storeMapping(mappingV1, EngineType.PROGUARD)
        val r2 = manager.storeMapping(mappingV2, EngineType.PROGUARD)

        val diff = manager.diffMappings(r1.id, r2.id)
        assertNotNull(diff)
        // com.example.New added; com.example.Baz + its field removed; Foo and Foo.bar changed
        assertEquals(1, diff!!.addedCount)
        assertEquals(2, diff.removedCount)
        assertEquals(2, diff.changedCount)
        assertTrue(diff.changeRate > 0.0)
    }

    @Test
    fun `diffMappings returns null for unknown ids`(@TempDir tempDir: Path) {
        val manager = MappingManager(tempDir.toString())
        assertNull(manager.diffMappings("nope", "also-nope"))
    }

    @Test
    fun `high change rate is detected`() {
        assertTrue(MappingDiff(0, 0, 4, 0.4).isHighChangeRate)
        assertFalse(MappingDiff(0, 0, 1, 0.1).isHighChangeRate)
    }
}
