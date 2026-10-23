package dev.pandasystems.fallingtrees.tree

import dev.pandasystems.fallingtrees.config.CommonConfig
import dev.pandasystems.fallingtrees.modId
import dev.pandasystems.pandalib.core.handles.Identifier
import dev.pandasystems.pandalib.core.handles.id
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.state.BlockState
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.max

class TreeScanContext(
	val level: BlockGetter,
	val origin: BlockPos,
	val definition: TreeDefinition,
	val config: CommonConfig,
	private val blacklist: BlockMatcher,
	private val effectiveLogs: BlockMatcher,
	private val effectiveLeaves: BlockMatcher
) {
	fun isBlacklisted(state: BlockState): Boolean = blacklist.matches(state)

	fun isLog(state: BlockState): Boolean =
		!blacklist.matches(state) && effectiveLogs.matches(state)

	fun isLeaf(state: BlockState): Boolean {
		if (blacklist.matches(state) || !effectiveLeaves.matches(state)) return false
		if (state.hasProperty(LeavesBlock.PERSISTENT) && state.getValue(LeavesBlock.PERSISTENT)) {
			return false
		}
		return true
	}

	fun isDecoration(state: BlockState): Boolean =
		!blacklist.matches(state) && definition.decorations.matches(state)

	fun isGround(state: BlockState): Boolean =
		TreeDetectors.UNIVERSAL_GROUND.matches(state)

	fun isTrunkAnchored(pos: BlockPos): Boolean {
		if (pos == origin) return false
		val belowPos = pos.below()
		if (belowPos == origin) return false
		val belowState = level.getBlockState(belowPos)
		if (isGround(belowState)) return true
		if (!config.scanBelowOrigin && belowPos.y < origin.y && isLog(belowState)) {
			return true
		}
		return false
	}
}

fun interface TreeDetector {
	fun scan(context: TreeScanContext): ScannedTree?
}

object TreeDetectors {
	private val detectors = ConcurrentHashMap<Identifier, TreeDetector>()

	val BRANCHING_ID: Identifier = id(modId, "branching")
	val CARDINAL_BRANCHING_ID: Identifier = id(modId, "cardinal_branching")
	val COLUMN_ID: Identifier = id(modId, "column")

	val UNIVERSAL_GROUND: BlockMatcher = BlockMatcher.ofTags(
		BlockTags.DIRT,
		BlockTags.SAND,
		BlockTags.MUD,
		BlockTags.MOSS_BLOCKS,
		BlockTags.NYLIUM,
		BlockTags.TERRACOTTA,
		BlockTags.SUPPORTS_VEGETATION,
		BlockTags.SUPPORTS_MANGROVE_PROPAGULE,
		BlockTags.SUPPORTS_CACTUS,
		BlockTags.SUPPORTS_BAMBOO,
		BlockTags.SUPPORTS_CHORUS_PLANT
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

	val NEIGHBOR_OFFSETS_26: List<BlockPos> = buildList {
		for (dir in Direction.entries) {
			add(BlockPos(dir.stepX, dir.stepY, dir.stepZ))
		}
		for (dy in -1..1) {
			for (dx in -1..1) {
				for (dz in -1..1) {
					val manhattan = abs(dx) + abs(dy) + abs(dz)
					if (manhattan > 1) {
						add(BlockPos(dx, dy, dz))
					}
				}
			}
		}
	}

	val NEIGHBOR_OFFSETS_6: List<BlockPos> = Direction.entries.map {
		BlockPos(it.stepX, it.stepY, it.stepZ)
	}

	init {
		register(BRANCHING_ID, BranchingTreeDetector)
		register(CARDINAL_BRANCHING_ID, CardinalBranchingDetector)
		register(COLUMN_ID, ColumnTreeDetector)
	}

	fun register(id: Identifier, detector: TreeDetector): TreeDetector {
		detectors[id] = detector
		return detector
	}

	fun get(id: Identifier): TreeDetector = detectors[id] ?: BranchingTreeDetector

	fun scanTrunks(
		context: TreeScanContext,
		offsets: List<BlockPos>
	): Pair<LinkedHashSet<BlockPos>, Int> {
		val maxTrunks = context.config.maxTrunkBlocks
		val visitedTrunks = LinkedHashSet<BlockPos>()
		val queue = ArrayDeque<BlockPos>()

		val start = context.origin.immutable()
		visitedTrunks.add(start)
		queue.addLast(start)

		var remainingGroundAnchors = 0

		while (queue.isNotEmpty() && visitedTrunks.size < maxTrunks) {
			val current = queue.removeFirst()

			if (context.isTrunkAnchored(current)) {
				remainingGroundAnchors++
			}

			for (offset in offsets) {
				val nextPos = current.offset(offset)
				if (!context.config.scanBelowOrigin && nextPos.y < context.origin.y) {
					continue
				}
				if (nextPos in visitedTrunks) continue

				val nextState = context.level.getBlockState(nextPos)
				if (context.isLog(nextState)) {
					val immutable = nextPos.immutable()
					visitedTrunks.add(immutable)
					queue.addLast(immutable)
					if (visitedTrunks.size >= maxTrunks) break
				}
			}
		}

		return visitedTrunks to remainingGroundAnchors
	}

	fun scanCanopyAndDecorations(
		context: TreeScanContext,
		visitedTrunks: Set<BlockPos>
	): Pair<List<BlockPos>, List<BlockPos>> {
		val maxLeaves = context.config.maxLeafBlocks
		val maxLeafDist = context.config.maxLeafDistance

		val candidateLeaves = LinkedHashMap<BlockPos, Int>()
		val rivalTrunks = LinkedHashSet<BlockPos>()

		if (context.definition.hasLeaves && maxLeafDist > 0 && maxLeaves > 0) {
			val leafQueue = ArrayDeque<Pair<BlockPos, Int>>()
			for (trunkPos in visitedTrunks) {
				leafQueue.addLast(trunkPos to 0)
			}

			while (leafQueue.isNotEmpty() && candidateLeaves.size < maxLeaves) {
				val (currentPos, dist) = leafQueue.removeFirst()
				if (dist >= maxLeafDist) continue

				for (offset in NEIGHBOR_OFFSETS_26) {
					val neighborPos = currentPos.offset(offset)
					if (neighborPos in visitedTrunks) continue

					// Diagonal steps only allowed if within Chebyshev distance of a trunk or cardinal step
					val isCardinal = abs(offset.x) + abs(offset.y) + abs(offset.z) == 1
					val stepCost = if (isCardinal) 1 else 2
					val nextDist = if (currentPos in visitedTrunks) 1 else dist + stepCost
					if (nextDist > maxLeafDist) continue

					val existingDist = candidateLeaves[neighborPos]
					if (existingDist != null && existingDist <= nextDist) continue

					val neighborState = context.level.getBlockState(neighborPos)
					if (context.isLeaf(neighborState)) {
						val immutableLeaf = neighborPos.immutable()
						candidateLeaves[immutableLeaf] = nextDist
						leafQueue.addLast(immutableLeaf to nextDist)
						if (candidateLeaves.size >= maxLeaves) break
					} else if (context.isLog(neighborState)) {
						rivalTrunks.add(neighborPos.immutable())
					}
				}
			}

			// Also check 1 block around the outer rim of candidate leaves for any rival trunks
			if (candidateLeaves.isNotEmpty()) {
				for (leafPos in candidateLeaves.keys) {
					for (offset in NEIGHBOR_OFFSETS_26) {
						val checkPos = leafPos.offset(offset)
						if (checkPos !in visitedTrunks && checkPos !in candidateLeaves && checkPos !in rivalTrunks) {
							if (context.isLog(context.level.getBlockState(checkPos))) {
								rivalTrunks.add(checkPos.immutable())
							}
						}
					}
				}
			}
		}

		// Phase 3: Multi-Source Voronoi filter against rival trunks
		val survivingLeaves = if (rivalTrunks.isEmpty() || candidateLeaves.isEmpty()) {
			LinkedHashSet(candidateLeaves.keys)
		} else {
			filterRivalLeaves(candidateLeaves, visitedTrunks, rivalTrunks)
		}

		// Phase 4: Collect attached & hanging decorations around trunks and surviving leaves
		val decorations = scanDecorations(context, visitedTrunks, survivingLeaves)

		return survivingLeaves.toList() to decorations
	}

	private fun filterRivalLeaves(
		candidateLeaves: Map<BlockPos, Int>,
		ourTrunks: Set<BlockPos>,
		rivalTrunks: Set<BlockPos>
	): LinkedHashSet<BlockPos> {
		val rivalDist = HashMap<BlockPos, Int>()
		val queue = ArrayDeque<Pair<BlockPos, Int>>()

		// Seed multi-source BFS from all rival trunks into our candidate leaf set
		for (rivalPos in rivalTrunks) {
			for (offset in NEIGHBOR_OFFSETS_26) {
				val adj = rivalPos.offset(offset)
				if (adj in candidateLeaves && adj !in rivalDist) {
					rivalDist[adj] = 1
					queue.addLast(adj to 1)
				}
			}
		}

		while (queue.isNotEmpty()) {
			val (current, dist) = queue.removeFirst()
			for (offset in NEIGHBOR_OFFSETS_26) {
				val next = current.offset(offset)
				if (next !in candidateLeaves) continue
				val isCardinal = abs(offset.x) + abs(offset.y) + abs(offset.z) == 1
				val nextDist = dist + if (isCardinal) 1 else 2
				val prev = rivalDist[next]
				if (prev == null || nextDist < prev) {
					rivalDist[next] = nextDist
					queue.addLast(next to nextDist)
				}
			}
		}

		// Keep leaves that belong to our tree
		val ownedLeaves = HashSet<BlockPos>()
		for ((leafPos, ourStepDist) in candidateLeaves) {
			val theirStepDist = rivalDist[leafPos]
			if (theirStepDist == null || ourStepDist < theirStepDist) {
				ownedLeaves.add(leafPos)
			} else if (ourStepDist == theirStepDist) {
				// Tie-breaker: compare 3D Euclidean squared distance to nearest trunk block
				val ourMinSq = minDistSq(leafPos, ourTrunks)
				val theirMinSq = minDistSq(leafPos, rivalTrunks)
				if (ourMinSq <= theirMinSq) {
					ownedLeaves.add(leafPos)
				}
			}
		}

		// Prune any leaf pockets that became disconnected from our trunks after rival removal
		val reachable = LinkedHashSet<BlockPos>()
		val reachQueue = ArrayDeque<BlockPos>()
		for (trunkPos in ourTrunks) {
			for (offset in NEIGHBOR_OFFSETS_26) {
				val adj = trunkPos.offset(offset)
				if (adj in ownedLeaves && reachable.add(adj)) {
					reachQueue.addLast(adj)
				}
			}
		}

		while (reachQueue.isNotEmpty()) {
			val current = reachQueue.removeFirst()
			for (offset in NEIGHBOR_OFFSETS_26) {
				val next = current.offset(offset)
				if (next in ownedLeaves && reachable.add(next)) {
					reachQueue.addLast(next)
				}
			}
		}

		return reachable
	}

	private fun minDistSq(pos: BlockPos, trunks: Set<BlockPos>): Double {
		var min = Double.MAX_VALUE
		for (t in trunks) {
			val d = pos.distSqr(t)
			if (d < min) min = d
		}
		return min
	}

	private fun scanDecorations(
		context: TreeScanContext,
		trunks: Set<BlockPos>,
		leaves: Set<BlockPos>
	): List<BlockPos> {
		if (context.definition.decorations === BlockMatcher.NONE) return emptyList()

		val visitedDecorations = LinkedHashSet<BlockPos>()
		val anchors = sequence {
			yieldAll(trunks)
			yieldAll(leaves)
		}

		for (anchor in anchors) {
			for (dir in Direction.entries) {
				val neighborPos = anchor.relative(dir)
				if (neighborPos in trunks || neighborPos in leaves || neighborPos in visitedDecorations) {
					continue
				}
				val state = context.level.getBlockState(neighborPos)
				if (context.isDecoration(state)) {
					val immutableDeco = neighborPos.immutable()
					visitedDecorations.add(immutableDeco)

					// Trace hanging decorations (vines, weeping vines, hanging moss, propagules) downward
					var downCursor = immutableDeco.below()
					var depth = 0
					while (depth < 32 && downCursor !in trunks && downCursor !in leaves && downCursor !in visitedDecorations) {
						val downState = context.level.getBlockState(downCursor)
						if (context.isDecoration(downState)) {
							visitedDecorations.add(downCursor.immutable())
							downCursor = downCursor.below()
							depth++
						} else {
							break
						}
					}
				}
			}
		}

		return visitedDecorations.toList()
	}
}

object BranchingTreeDetector : TreeDetector {
	override fun scan(context: TreeScanContext): ScannedTree? {
		val originState = context.level.getBlockState(context.origin)
		if (!context.isLog(originState)) return null

		val (trunks, remainingAnchors) = TreeDetectors.scanTrunks(context, TreeDetectors.NEIGHBOR_OFFSETS_26)
		val (leaves, decorations) = TreeDetectors.scanCanopyAndDecorations(context, trunks)

		if (context.definition.hasLeaves && leaves.size < context.config.minLeavesRequired) {
			return null
		}

		return ScannedTree(
			origin = context.origin.immutable(),
			definition = context.definition,
			trunkPositions = trunks.toList(),
			leafPositions = leaves,
			decorationPositions = decorations,
			remainingGroundAnchors = remainingAnchors
		)
	}
}

object CardinalBranchingDetector : TreeDetector {
	override fun scan(context: TreeScanContext): ScannedTree? {
		val originState = context.level.getBlockState(context.origin)
		if (!context.isLog(originState)) return null

		val (trunks, remainingAnchors) = TreeDetectors.scanTrunks(context, TreeDetectors.NEIGHBOR_OFFSETS_6)
		val (leaves, decorations) = TreeDetectors.scanCanopyAndDecorations(context, trunks)

		if (context.definition.hasLeaves && leaves.isEmpty()) {
			return null
		}

		return ScannedTree(
			origin = context.origin.immutable(),
			definition = context.definition,
			trunkPositions = trunks.toList(),
			leafPositions = leaves,
			decorationPositions = decorations,
			remainingGroundAnchors = remainingAnchors
		)
	}
}

object ColumnTreeDetector : TreeDetector {
	override fun scan(context: TreeScanContext): ScannedTree? {
		val originState = context.level.getBlockState(context.origin)
		if (!context.isLog(originState)) return null

		val maxTrunks = context.config.maxTrunkBlocks
		val trunks = mutableListOf<BlockPos>()
		val decorations = mutableListOf<BlockPos>()
		val originImmutable = context.origin.immutable()
		trunks.add(originImmutable)

		var cursor = context.origin.above()
		while (trunks.size < maxTrunks) {
			val state = context.level.getBlockState(cursor)
			if (context.isLog(state)) {
				trunks.add(cursor.immutable())
				cursor = cursor.above()
			} else if (context.isDecoration(state)) {
				decorations.add(cursor.immutable())
				break
			} else {
				break
			}
		}

		var remainingGroundAnchors = 0
		if (context.config.scanBelowOrigin) {
			var downCursor = context.origin.below()
			while (trunks.size < maxTrunks) {
				val state = context.level.getBlockState(downCursor)
				if (context.isLog(state)) {
					val immutableDown = downCursor.immutable()
					trunks.add(immutableDown)
					if (context.isGround(context.level.getBlockState(downCursor.below()))) {
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
			definition = context.definition,
			trunkPositions = trunks,
			leafPositions = emptyList(),
			decorationPositions = decorations,
			remainingGroundAnchors = remainingGroundAnchors
		)
	}
}
