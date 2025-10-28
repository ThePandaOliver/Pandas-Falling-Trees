/*
 * Copyright (C) 2025 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.pandasystems.fallingtrees.api

import com.mojang.logging.LogUtils
import dev.pandasystems.fallingtrees.config.ClientConfig
import dev.pandasystems.fallingtrees.config.MiningOptionEnum
import dev.pandasystems.fallingtrees.config.fallingTreesClientConfig
import dev.pandasystems.fallingtrees.config.fallingTreesCommonConfig
import dev.pandasystems.fallingtrees.entity.TreeEntity
import dev.pandasystems.fallingtrees.exceptions.TreeException
import dev.pandasystems.fallingtrees.getTree
import dev.pandasystems.fallingtrees.treeEntity
import dev.pandasystems.pandalib.config.getSynced
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.stats.Stats
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import org.slf4j.Logger
import java.awt.Color
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

object TreeHandler {
	private val LOGGER: Logger = LogUtils.getLogger()

	val TREE_SPEED_CACHES: MutableMap<UUID, TreeSpeed> = ConcurrentHashMap<UUID, TreeSpeed>()

	@JvmStatic
	fun destroyTree(level: Level, blockPos: BlockPos, player: Player): Boolean {
		if (level.isClientSide()) return false
		val blockState = level.getBlockState(blockPos)

		val tree = getTree(blockState) ?: return false

		val data = tryGatherTreeData(tree, blockPos, level, player, false) ?: return false
		val blocks = data.blocks

		val entity = TreeEntity(treeEntity.get(), level)
		entity.setPos(blockPos.x + 0.5, blockPos.y.toDouble(), blockPos.z + 0.5)
		entity.setData(player, tree, blockPos, blocks, data.drops)

		player.causeFoodExhaustion(
			if (fallingTreesCommonConfig.get().disableExtraFoodExhaustion) 0.005f else data.foodExhaustionModifier.getExhaustion(0.005f)
		)

		if (player.mainHandItem.isDamageableItem) player.mainHandItem.hurtAndBreak(
			if (fallingTreesCommonConfig.get().disableExtraToolDamage) 1 else data.toolDamage,
			player, EquipmentSlot.MAINHAND
		)

		player.awardStat(Stats.ITEM_USED.get(player.mainHandItem.item))
		data.awardedStats.forEach { awardedStat -> player.awardStat(awardedStat.stat, awardedStat.amount) }


		blocks.forEach { pos ->
			// Remove all blocks but don't update neighbors
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3, 0)
		}

		// Make sure all neighboring blocks have been updated after all has been removed
		blocks.forEach { pos ->
			level.updateNeighborsAt(pos, Blocks.AIR)
		}

		level.addFreshEntity(entity)
		return true
	}

	fun tryGatherTreeData(treeType: TreeType, blockPos: BlockPos, level: Level, player: Player, ignoreExceptions: Boolean): TreeData? {
		try {
			return treeType.gatherTreeData(blockPos, level, player)
		} catch (e: TreeException) {
			if (!ignoreExceptions) {
				LOGGER.warn(e.message)
			}
		} catch (e: Exception) {
			if (!ignoreExceptions) {
				LOGGER.error("An error occurred when trying to gather tree data", e)
				player.displayClientMessage(Component.literal("Error: $e").withStyle(Style.EMPTY.withColor(Color.red.rgb)), false)
				player.displayClientMessage(
					Component.translatable("text.fallingtrees.tree_handler.exception.1").withStyle(Style.EMPTY.withColor(Color.red.rgb)), false
				)
				player.displayClientMessage(
					Component.translatable("text.fallingtrees.tree_handler.exception.2").withStyle(Style.EMPTY.withColor(Color.red.rgb)), false
				)
			}
		}
		return null
	}

	@JvmStatic
	fun canPlayerChopTree(player: Player): Boolean {
		val option = if (player.isCrouching) fallingTreesClientConfig.getSynced(ClientConfig::miningWhileCrouchingShould)[player]
		else fallingTreesClientConfig.getSynced(ClientConfig::miningShould)[player]
		return fallingTreesCommonConfig.get().disableCrouchMining || option == MiningOptionEnum.CHOP_TREE
	}

	@JvmStatic
	fun getMiningSpeed(player: Player, blockPos: BlockPos, baseSpeed: Float): Optional<Float?> {
		val treeSpeed = TREE_SPEED_CACHES.compute(player.getUUID()) { _: UUID?, speed: TreeSpeed? ->
			if (speed == null || !speed.isValid(blockPos, baseSpeed)) {
				val blockState = player.level().getBlockState(blockPos)
				val tree = getTree(blockState) ?: return@compute null
				val data: TreeData = tryGatherTreeData(tree, blockPos, player.level(), player, true) ?: return@compute null
				return@compute TreeSpeed(baseSpeed, data.miningSpeedModifier.getMiningSpeed(baseSpeed), blockPos.immutable())
			}
			speed
		}
		return Optional.ofNullable<TreeSpeed?>(treeSpeed).map<Float?>(Function { obj: TreeSpeed? -> obj!!.miningSpeed })
	}

	class TreeSpeed(private val baseMiningSpeed: Float, val miningSpeed: Float, private val blockPos: BlockPos?) {
		fun isValid(blockPos: BlockPos?, baseSpeed: Float): Boolean {
			return this.blockPos == blockPos && this.baseMiningSpeed == baseSpeed
		}
	}
}
