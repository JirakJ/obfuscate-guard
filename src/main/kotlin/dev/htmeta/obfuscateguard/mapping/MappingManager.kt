package dev.htmeta.obfuscateguard.mapping

import com.intellij.openapi.diagnostic.Logger
import dev.htmeta.obfuscateguard.core.model.EngineType
import dev.htmeta.obfuscateguard.core.model.MappingRecord
import dev.htmeta.obfuscateguard.core.model.MappingStats
import dev.htmeta.obfuscateguard.core.spi.MappingEntry
import dev.htmeta.obfuscateguard.core.spi.MappingEntryType
import dev.htmeta.obfuscateguard.core.spi.ObfuscationEngineAdapter
import java.io.File
import java.util.UUID

/**
 * Manages mapping files — store, retrieve, diff, and export.
 */
class MappingManager(private val storagePath: String) {

    private val records = mutableListOf<MappingRecord>()

    fun storageDir(): File = File(storagePath).apply { mkdirs() }

    fun storeMapping(
        mappingContent: String,
        engineType: EngineType,
        gitCommitHash: String? = null,
        buildVariant: String? = null,
    ): MappingRecord {
        val id = UUID.randomUUID().toString()
        val file = File(storageDir(), "$id.txt")
        file.writeText(mappingContent)

        val adapter = ObfuscationEngineAdapter.findAdapter(engineType)
        val entries = adapter?.parseMapping(mappingContent) ?: emptyList()

        val record = MappingRecord(
            id = id,
            timestamp = System.currentTimeMillis(),
            gitCommitHash = gitCommitHash,
            buildVariant = buildVariant,
            engineType = engineType,
            mappingFilePath = file.absolutePath,
            stats = MappingStats(
                classCount = entries.count { it.type == MappingEntryType.CLASS },
                methodCount = entries.count { it.type == MappingEntryType.METHOD },
                fieldCount = entries.count { it.type == MappingEntryType.FIELD },
            ),
        )
        records.add(record)
        LOG.info("Stored mapping $id: ${record.stats}")
        return record
    }

    fun listRecords(): List<MappingRecord> = records.toList()

    fun getMapping(id: String): String? {
        val record = records.find { it.id == id } ?: return null
        return try { File(record.mappingFilePath).readText() } catch (_: Exception) { null }
    }

    fun diffMappings(id1: String, id2: String): MappingDiff? {
        val content1 = getMapping(id1) ?: return null
        val content2 = getMapping(id2) ?: return null

        val adapter = records.find { it.id == id1 }?.let {
            ObfuscationEngineAdapter.findAdapter(it.engineType)
        } ?: return null

        val entries1 = adapter.parseMapping(content1).associateBy { it.originalName }
        val entries2 = adapter.parseMapping(content2).associateBy { it.originalName }

        val added = entries2.keys - entries1.keys
        val removed = entries1.keys - entries2.keys
        val changed = entries1.keys.intersect(entries2.keys).filter {
            entries1[it]?.obfuscatedName != entries2[it]?.obfuscatedName
        }

        return MappingDiff(
            addedCount = added.size,
            removedCount = removed.size,
            changedCount = changed.size,
            changeRate = if (entries1.isNotEmpty()) changed.size.toDouble() / entries1.size else 0.0,
        )
    }

    companion object {
        private val LOG = Logger.getInstance(MappingManager::class.java)
    }
}

data class MappingDiff(
    val addedCount: Int,
    val removedCount: Int,
    val changedCount: Int,
    val changeRate: Double,
) {
    val isHighChangeRate: Boolean get() = changeRate > 0.3
}
