package dev.pandasystems.fallingtrees.tree

import dev.pandasystems.fallingtrees.config.CommonConfig
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.state.BlockState
import java.util.concurrent.ConcurrentHashMap

object TreeScanCache {
	private const val CACHE_TTL_MS = 400L
	private const val MAX_CACHE_ENTRIES = 64

	private data class CacheKey(
		val levelIdentity: Int,
		val posLong: Long
	)

	private data class CacheEntry(
		val originState: BlockState,
		val timestampMs: Long,
		val scannedTree: ScannedTree?
	)

	private val cache = ConcurrentHashMap<CacheKey, CacheEntry>()

	fun getOrScan(level: BlockGetter, pos: BlockPos, config: CommonConfig): ScannedTree? {
		val currentState = level.getBlockState(pos)
		val now = System.currentTimeMillis()
		val key = CacheKey(System.identityHashCode(level), pos.asLong())

		val existing = cache[key]
		if (existing != null && existing.originState === currentState && (now - existing.timestampMs) <= CACHE_TTL_MS) {
			return existing.scannedTree
		}

		if (cache.size >= MAX_CACHE_ENTRIES) {
			cache.entries.removeIf { (now - it.value.timestampMs) > CACHE_TTL_MS }
			if (cache.size >= MAX_CACHE_ENTRIES) {
				cache.clear()
			}
		}

		val scanned = TreeRegistry.scanTree(level, pos, config)
		cache[key] = CacheEntry(currentState, now, scanned)
		return scanned
	}

	fun invalidate(level: BlockGetter, pos: BlockPos) {
		val key = CacheKey(System.identityHashCode(level), pos.asLong())
		cache.remove(key)
	}

	fun clear() {
		cache.clear()
	}
}
