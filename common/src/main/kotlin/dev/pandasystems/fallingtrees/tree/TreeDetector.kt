package dev.pandasystems.fallingtrees.tree

import dev.pandasystems.fallingtrees.config.CommonConfig
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.state.BlockState

object DefaultGroundMatchers {
	val STANDARD_GROUND: BlockMatcher = BlockMatcher.ofTags(
		BlockTags.DIRT,
		BlockTags.SAND,
		BlockTags.MUD,
		BlockTags.MOSS_BLOCKS,
		BlockTags.NYLIUM,
		BlockTags.TERRACOTTA,
		BlockTags.SUPPORTS_VEGETATION,
		BlockTags.SUPPORTS_MANGROVE_PROPAGULE
	) or BlockMatcher.ofBlocks(
		Blocks.NETHERRACK,
		Blocks.SOUL_SAND,
		Blocks.SOUL_SOIL,
		Blocks.END_STONE,
		Blocks.GRAVEL,
		Blocks.CLAY,
		Blocks.FARMLAND,
		Blocks.MYCELIUM,
		Blocks.PODZOL,
		Blocks.ROOTED_DIRT,
		Blocks.MUDDY_MANGROVE_ROOTS
	)

	val CACTUS_GROUND: BlockMatcher =
		BlockMatcher.ofTags(BlockTags.SUPPORTS_CACTUS, BlockTags.SAND) or STANDARD_GROUND

	val BAMBOO_GROUND: BlockMatcher =
		BlockMatcher.ofTags(BlockTags.SUPPORTS_BAMBOO, BlockTags.DIRT, BlockTags.SAND) or STANDARD_GROUND

	val CHORUS_GROUND: BlockMatcher =
		BlockMatcher.ofTags(BlockTags.SUPPORTS_CHORUS_PLANT) or BlockMatcher.ofBlocks(Blocks.END_STONE)
}

fun interface TreeDetector {
	fun scan(
		level: BlockGetter,
		origin: BlockPos,
		definition: TreeDefinition,
		config: CommonConfig,
		blacklist: BlockMatcher
	): ScannedTree?
}

object StandardTreeDetector : TreeDetector {
	private val NEIGHBOR_OFFSETS_26: List<BlockPos> = buildList {
		for (dy in -1..1) {
			for (dx in -1..1) {
				for (dz in -1..1) {
					if (dx != 0 || dy != 0 || dz != 0) {
						add(BlockPos(dx, dy, dz))
					}
				}
			}
		}
	}

	private val NEIGHBOR_OFFSETS_6: List<BlockPos> = Direction.entries.map {
		BlockPos(it.stepX, it.stepY, it.stepZ)
	}

	override fun scan(
		level: BlockGetter,
		origin: BlockPos,
		definition: TreeDefinition,
		config: CommonConfig,
		blacklist: BlockMatcher
	): ScannedTree? {
		val originState = level.getBlockState(origin)
		if (blacklist.matches(originState) || !definition.trunkMatcher.matches(originState)) {
			return null
		}

		val maxTrunks = definition.resolveMaxTrunkBlocks(config)
		val maxLeaves = definition.resolveMaxLeafBlocks(config)
		val maxLeafDist = definition.resolveMaxLeafDistance(config)
		val minLeaves = definition.resolveMinLeaves(config)

		val extraLogs = BlockMatcher.fromConfigStrings(config.extraLogBlocks)
		val extraLeaves = BlockMatcher.fromConfigStrings(config.extraLeafBlocks)

		val effectiveTrunkMatcher = definition.trunkMatcher or extraLogs
		val effectiveLeafMatcher = definition.leafMatcher or extraLeaves

		val trunkOffsets = if (definition.allowDiagonalTrunks) NEIGHBOR_OFFSETS_26 else NEIGHBOR_OFFSETS_6

		val visitedTrunks = LinkedHashSet<BlockPos>()
		val trunkQueue = ArrayDeque<BlockPos>()
		val initialPosition = origin.immutable()
		visitedTrunks.add(initialPosition)
		trunkQueue.addLast(initialPosition)

		var remainingGroundAnchors = 0

		while (trunkQueue.isNotEmpty() && visitedTrunks.size < maxTrunks) {
			val current = trunkQueue.removeFirst()

			// Check if this non-origin trunk block is still anchored to the ground or to an uncut lower trunk
			if (current != origin) {
				val belowPos = current.below()
				if (belowPos != origin) {
					val belowState = level.getBlockState(belowPos)
					val anchoredToGround = definition.groundMatcher.matches(belowState)
					val anchoredToUncutStump = !config.scanBelowOrigin &&
						belowPos.y < origin.y &&
						effectiveTrunkMatcher.matches(belowState) &&
						!blacklist.matches(belowState)

					if (anchoredToGround || anchoredToUncutStump) {
						remainingGroundAnchors++
					}
				}
			}

			for (offset in trunkOffsets) {
				val nextPos = current.offset(offset)
				if (!config.scanBelowOrigin && nextPos.y < origin.y) {
					continue
				}
				if (nextPos in visitedTrunks) {
					continue
				}

				val nextState = level.getBlockState(nextPos)
				if (!blacklist.matches(nextState) && effectiveTrunkMatcher.matches(nextState)) {
					val immutableNext = nextPos.immutable()
					visitedTrunks.add(immutableNext)
					trunkQueue.addLast(immutableNext)
					if (visitedTrunks.size >= maxTrunks) {
						break
					}
				}
			}
		}

		// Scan connected leaves and decorations around the discovered trunks
		val visitedLeaves = LinkedHashSet<BlockPos>()
		val visitedDecorations = LinkedHashSet<BlockPos>()

		if (maxLeafDist > 0 && maxLeaves > 0) {
			val leafQueue = ArrayDeque<Pair<BlockPos, Int>>()
			for (trunkPos in visitedTrunks) {
				leafQueue.addLast(trunkPos to 0)
			}

			while (leafQueue.isNotEmpty() && visitedLeaves.size < maxLeaves) {
				val (currentPos, distance) = leafQueue.removeFirst()
				if (distance >= maxLeafDist) continue

				for (direction in Direction.entries) {
					val neighborPos = currentPos.relative(direction)
					if (neighborPos in visitedTrunks || neighborPos in visitedLeaves || neighborPos in visitedDecorations) {
						continue
					}

					val neighborState = level.getBlockState(neighborPos)
					if (blacklist.matches(neighborState)) continue

					if (isMatchingLeaf(neighborState, effectiveLeafMatcher, definition.requireNaturalLeaves)) {
						val immutableLeaf = neighborPos.immutable()
						visitedLeaves.add(immutableLeaf)
						leafQueue.addLast(immutableLeaf to (distance + 1))
						if (visitedLeaves.size >= maxLeaves) break
					} else if (definition.decorationMatcher.matches(neighborState)) {
						visitedDecorations.add(neighborPos.immutable())
					}
				}
			}
		}

		if (visitedLeaves.size < minLeaves) {
			return null
		}

		return ScannedTree(
			origin = initialPosition,
			definition = definition,
			trunkPositions = visitedTrunks.toList(),
			leafPositions = visitedLeaves.toList(),
			decorationPositions = visitedDecorations.toList(),
			remainingGroundAnchors = remainingGroundAnchors
		)
	}

	private fun isMatchingLeaf(
		state: BlockState,
		leafMatcher: BlockMatcher,
		requireNatural: Boolean
	): Boolean {
		if (!leafMatcher.matches(state)) return false
		if (requireNatural && state.hasProperty(LeavesBlock.PERSISTENT)) {
			if (state.getValue(LeavesBlock.PERSISTENT)) {
				return false
			}
		}
		return true
	}
}

object VerticalColumnDetector : TreeDetector {
	override fun scan(
		level: BlockGetter,
		origin: BlockPos,
		definition: TreeDefinition,
		config: CommonConfig,
		blacklist: BlockMatcher
	): ScannedTree? {
		val originState = level.getBlockState(origin)
		if (blacklist.matches(originState) || !definition.trunkMatcher.matches(originState)) {
			return null
		}

		val maxTrunks = definition.resolveMaxTrunkBlocks(config)
		val trunks = mutableListOf<BlockPos>()
		val decorations = mutableListOf<BlockPos>()
		val originImmutable = origin.immutable()
		trunks.add(originImmutable)

		// Scan upwards from origin
		var cursor = origin.above()
		while (trunks.size < maxTrunks) {
			val state = level.getBlockState(cursor)
			if (blacklist.matches(state)) break
			if (definition.trunkMatcher.matches(state)) {
				trunks.add(cursor.immutable())
				cursor = cursor.above()
			} else if (definition.decorationMatcher.matches(state)) {
				decorations.add(cursor.immutable())
				break
			} else {
				break
			}
		}

		var remainingGroundAnchors = 0
		if (config.scanBelowOrigin) {
			var downCursor = origin.below()
			while (trunks.size < maxTrunks) {
				val state = level.getBlockState(downCursor)
				if (!blacklist.matches(state) && definition.trunkMatcher.matches(state)) {
					val immutableDown = downCursor.immutable()
					trunks.add(immutableDown)
					val belowState = level.getBlockState(downCursor.below())
					if (definition.groundMatcher.matches(belowState)) {
						remainingGroundAnchors++
					}
					downCursor = downCursor.below()
				} else {
					break
				}
			}
		}

		return ScannedTree(
			origin = originImmutable,
			definition = definition,
			trunkPositions = trunks,
			leafPositions = emptyList(),
			decorationPositions = decorations,
			remainingGroundAnchors = remainingGroundAnchors
		)
	}
}
