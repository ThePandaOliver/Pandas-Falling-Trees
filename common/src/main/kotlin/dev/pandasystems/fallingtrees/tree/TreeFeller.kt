package dev.pandasystems.fallingtrees.tree

import dev.pandasystems.fallingtrees.config.CommonConfig
import dev.pandasystems.fallingtrees.config.fallingTreesCommonConfig
import dev.pandasystems.fallingtrees.FallingTreesRegistries
import dev.pandasystems.fallingtrees.entity.TreeEntity
import dev.pandasystems.pandalib.event.events.server.ServerPlayerBlockBreakEventContext
import dev.pandasystems.pandalib.event.events.server.ServerPlayerEvents
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.ItemTags
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow

object TreeFeller {
	private val isFellingTree: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }

	private data class SessionKey(
		val playerUuid: UUID,
		val isClientSide: Boolean
	)

	private data class MiningSession(
		val pos: BlockPos,
		var lastTick: Int,
		val initialItem: Item,
		val startedCrouching: Boolean
	)

	private val miningSessions = ConcurrentHashMap<SessionKey, MiningSession>()
	private const val MAX_TICK_GAP = 10

	fun init() {
		TreeRegistry.registerDefaults()
		ServerPlayerEvents.playerBlockBreakBefore.subscribe(::onBeforeBlockBreak)
	}

	fun isPlayerCrouching(player: Player): Boolean {
		return player.isShiftKeyDown || player.isCrouching
	}

	private fun getOrStartMiningSession(player: Player, pos: BlockPos, isClientSide: Boolean): MiningSession {
		val key = SessionKey(player.uuid, isClientSide)
		val currentTick = player.tickCount
		val currentItem = player.mainHandItem.item
		val existing = miningSessions[key]

		if (existing != null &&
			existing.pos == pos &&
			existing.initialItem == currentItem &&
			(currentTick - existing.lastTick) in 0..MAX_TICK_GAP
		) {
			existing.lastTick = currentTick
			return existing
		}

		val isCrouching = isPlayerCrouching(player)
		val newSession = MiningSession(
			pos = pos.immutable(),
			lastTick = currentTick,
			initialItem = currentItem,
			startedCrouching = isCrouching
		)
		miningSessions[key] = newSession

		if (miningSessions.size > 64) {
			miningSessions.entries.removeIf { (_, s) ->
				val gap = currentTick - s.lastTick
				gap > MAX_TICK_GAP || gap < 0
			}
		}

		return newSession
	}

	fun canPlayerTriggerFell(
		player: Player,
		config: CommonConfig,
		pos: BlockPos? = null
	): Boolean {
		val isCrouching = if (pos != null) {
			val session = getOrStartMiningSession(player, pos, player.level().isClientSide)
			session.startedCrouching
		} else {
			isPlayerCrouching(player)
		}

		if (!config.disableCrouchMining) {
			if (isCrouching != config.reverseCrouchMining) {
				return false
			}
		}

		val mainHandItem = player.mainHandItem
		if (config.requireAxe) {
			val isAxe = mainHandItem.`is`(ItemTags.AXES)
			val isCustomAllowed = BlockMatcher.matchesItemConfigStrings(mainHandItem, config.customAllowedTools)
			if (!isAxe && !isCustomAllowed) {
				return false
			}
		}

		if (!config.disableExtraToolDamage && config.preserveToolBeforeBreak && mainHandItem.isDamageableItem) {
			val remainingDurability = mainHandItem.maxDamage - mainHandItem.damageValue
			if (remainingDurability <= 1) return false
		}

		return true
	}

	@JvmStatic
	fun modifyDestroyProgress(
		baseProgress: Float,
		state: BlockState,
		player: Player,
		level: BlockGetter,
		pos: BlockPos
	): Float {
		if (baseProgress <= 0f) return baseProgress

		val config = fallingTreesCommonConfig.value
		if (TreeRegistry.findMatchingDefinitions(state, config).isEmpty()) return baseProgress

		val canFell = canPlayerTriggerFell(player, config, pos)
		if (!config.scaleMiningSpeedWithSize) return baseProgress
		if (!canFell) return baseProgress

		val scanned = TreeScanCache.getOrScan(level, pos, config) ?: return baseProgress
		if (!scanned.shouldFall(config)) return baseProgress

		val extraTrunks = (scanned.totalTrunkBlocks - 1).coerceAtLeast(0).toDouble()
		if (extraTrunks <= 0.0) return baseProgress

		val penalty = config.miningSpeedPenaltyPerBlock.toDouble() *
				extraTrunks.pow(config.miningSpeedExponent.toDouble())
		val multiplier = (1.0 + penalty).coerceIn(1.0, config.maxMiningTimeMultiplier.toDouble()).toFloat()

		return baseProgress / multiplier
	}

	private fun onBeforeBlockBreak(context: ServerPlayerBlockBreakEventContext) {
		if (isFellingTree.get()) return

		val serverLevel = context.level as? ServerLevel ?: return
		val player = context.player
		val origin = context.pos
		val config = fallingTreesCommonConfig.value

		TreeScanCache.invalidate(serverLevel, origin)

		val canFell = canPlayerTriggerFell(player, config, origin)
		miningSessions.remove(SessionKey(player.uuid, false))

		if (!canFell) return

		val scanned = TreeRegistry.scanTree(serverLevel, origin, config) ?: return
		if (!scanned.shouldFall(config)) return

		val tool = player.mainHandItem
		val maxTrunksByDurability = computeMaxFellableTrunks(tool, scanned.trunkPositions.size, player, config)
		if (maxTrunksByDurability <= 1 && scanned.leafPositions.isEmpty()) {
			return
		}

		val trunksToFell = scanned.trunkPositions.take(maxTrunksByDurability)
		val leavesToFell = if (config.breakLeavesInstantly) scanned.leafPositions else emptyList()
		val decorationsToFell = scanned.decorationPositions

		isFellingTree.set(true)
		try {
			fellScannedTree(
				serverLevel = serverLevel,
				player = player,
				origin = origin,
				trunks = trunksToFell,
				leaves = leavesToFell,
				decorations = decorationsToFell,
				tool = tool,
				config = config
			)
		} finally {
			isFellingTree.set(false)
		}
	}

	private fun computeMaxFellableTrunks(
		tool: ItemStack,
		requestedTrunks: Int,
		player: Player,
		config: CommonConfig
	): Int {
		if (player.isCreative || config.disableExtraToolDamage || !tool.isDamageableItem) {
			return requestedTrunks
		}
		val remaining = tool.maxDamage - tool.damageValue
		val usable = if (config.preserveToolBeforeBreak) (remaining - 1).coerceAtLeast(0) else remaining
		return requestedTrunks.coerceAtMost(usable.coerceAtLeast(1))
	}

	private fun fellScannedTree(
		serverLevel: ServerLevel,
		player: Player,
		origin: BlockPos,
		trunks: List<BlockPos>,
		leaves: List<BlockPos>,
		decorations: List<BlockPos>,
		tool: ItemStack,
		config: CommonConfig
	) {
		val capturedBlocks = LinkedHashMap<BlockPos, BlockState>()
		val collectedDrops = mutableListOf<Pair<BlockPos, ItemStack>>()

		val allPositions = LinkedHashSet<BlockPos>().apply {
			addAll(trunks)
			addAll(leaves)
			addAll(decorations)
		}

		val canDropItems = !player.isCreative
		val toolSnapshot = tool.copy()

		// Capture block states and loot drops before removing blocks
		for (pos in allPositions) {
			val state = serverLevel.getBlockState(pos)
			if (state.isAir) continue
			capturedBlocks[pos] = state

			// The origin block itself will be broken by vanilla's destroyBlock flow,
			// so we only collect drops for the additional blocks above/around origin.
			if (pos != origin && canDropItems) {
				val blockEntity = serverLevel.getBlockEntity(pos)
				val drops = Block.getDrops(state, serverLevel, pos, blockEntity, player, toolSnapshot)
				for (drop in drops) {
					if (!drop.isEmpty) {
						collectedDrops.add(pos to drop)
					}
				}
			}
		}

		val removalFlags = Block.UPDATE_CLIENTS or Block.UPDATE_KNOWN_SHAPE or Block.UPDATE_SUPPRESS_DROPS
		for (pos in allPositions) {
			val state = serverLevel.getBlockState(pos)
			if (!state.isAir) {
				val replacement = state.fluidState.createLegacyBlock()
				// Update limit is 0, so no neighbor updates are triggered.
				serverLevel.setBlock(pos, replacement, removalFlags, 0)
			}
		}

		// Notify neighbors around the cleared volume once removal is complete
		val updateNeighborFlags = removalFlags or -34
		for (pos in allPositions) {
			val oldState = capturedBlocks[pos]
			val newState = serverLevel.getBlockState(pos)
			oldState?.updateIndirectNeighbourShapes(serverLevel, pos, updateNeighborFlags, 511)
			newState.updateNeighbourShapes(serverLevel, pos, updateNeighborFlags, 511)
			newState.updateIndirectNeighbourShapes(serverLevel, pos, updateNeighborFlags, 511)
		}

		// Apply extra durability and exhaustion costs for additional trunk blocks
		val extraTrunksBroken = (trunks.size - 1).coerceAtLeast(0)
		if (!player.isCreative && extraTrunksBroken > 0) {
			if (!config.disableExtraToolDamage && tool.isDamageableItem) {
				tool.hurtAndBreak(extraTrunksBroken, player, InteractionHand.MAIN_HAND)
			}
			if (!config.disableExtraFoodExhaustion && config.exhaustionPerBlock > 0f) {
				player.causeFoodExhaustion(extraTrunksBroken * config.exhaustionPerBlock)
			}
		}

		// Spawn the TreeEntity to animate the fall and drop items on impact
		val treeEntity = TreeEntity(FallingTreesRegistries.treeEntity.get(), serverLevel)
		val fallDir = player.lookAngle
		treeEntity.initializeTree(
			origin = origin,
			blocks = capturedBlocks,
			drops = collectedDrops,
			fallDir = Vec3(fallDir.x, 0.0, fallDir.z)
		)
		serverLevel.addFreshEntity(treeEntity)

		serverLevel.playSound(
			null,
			origin,
			FallingTreesRegistries.treeFallSound.get(),
			SoundSource.BLOCKS,
			1.0f,
			0.9f + serverLevel.random.nextFloat() * 0.2f
		)
	}
}
