/*
 * Copyright (C) 2026 Oliver Froberg (The Panda Oliver)
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

import net.minecraft.core.BlockPos
import net.minecraft.stats.Stat
import net.minecraft.world.item.ItemStack
import java.util.*

@JvmRecord
data class TreeData(
	val blocks: MutableList<BlockPos>,
	val drops: MutableList<ItemStack>,
	val awardedStats: MutableList<AwardedStat>,
	val toolDamage: Int,
	val miningSpeedModifier: MiningSpeedModifier,
	val foodExhaustionModifier: FoodExhaustionModifier
) {
	class Builder {
		private val blocks = mutableListOf<BlockPos>()
		private val viewBlocks = Collections.unmodifiableList(blocks)
		private val drops = mutableListOf<ItemStack>()
		private val viewDrops = Collections.unmodifiableList(drops)
		private val awardedStats = mutableMapOf<Stat<*>, Int>()
		private var toolDamage = 0
		private var miningSpeedModifier = MiningSpeedModifier { originalMiningSpeed: Float -> originalMiningSpeed }
		private var foodExhaustionModifier = FoodExhaustionModifier { originalExhaustion: Float -> originalExhaustion }

		fun addBlock(blockPos: BlockPos): Builder {
			this.blocks.add(blockPos)
			return this
		}

		fun addBlocks(blockPos: BlockPos, vararg otherBlocks: BlockPos): Builder {
			this.blocks.add(blockPos)
			this.blocks.addAll(otherBlocks)
			return this
		}

		fun addBlocks(blocks: MutableCollection<BlockPos>): Builder {
			this.blocks.addAll(blocks)
			return this
		}

		fun addDrop(drop: ItemStack): Builder {
			this.drops.add(drop)
			return this
		}

		fun addDrops(drop: ItemStack, vararg otherDrops: ItemStack): Builder {
			this.drops.add(drop)
			this.drops.addAll(otherDrops)
			return this
		}

		fun addDrops(drops: MutableCollection<ItemStack>): Builder {
			this.drops.addAll(drops)
			return this
		}

		@JvmOverloads
		fun addAwardedStat(stat: Stat<*>, amount: Int = 1): Builder {
			this.awardedStats.compute(stat) { stat1: Stat<*>, oldAmount: Int? -> if (oldAmount == null) amount else oldAmount + amount }
			return this
		}

		fun <T: Any> addAwardedStats(stats: MutableCollection<Stat<T>>): Builder {
			stats.forEach { stat: Stat<T> -> this.addAwardedStat(stat) }
			return this
		}

		fun addAwardedStatsMap(stats: MutableMap<Stat<*>, Int>): Builder {
			this.awardedStats.putAll(stats)
			return this
		}

		fun setToolDamage(toolDamage: Int): Builder {
			this.toolDamage = toolDamage
			return this
		}

		fun setMiningSpeedModifier(miningSpeedModifier: MiningSpeedModifier): Builder {
			this.miningSpeedModifier = miningSpeedModifier
			return this
		}

		fun setFoodExhaustionModifier(foodExhaustionModifier: FoodExhaustionModifier): Builder {
			this.foodExhaustionModifier = foodExhaustionModifier
			return this
		}

		fun build(): TreeData {
			return TreeData(
				viewBlocks,
				viewDrops,
				awardedStats.entries.stream()
					.map { entry: MutableMap.MutableEntry<Stat<*>, Int> -> AwardedStat(entry.key, entry.value) }
					.toList(),
				toolDamage,
				miningSpeedModifier,
				foodExhaustionModifier
			)
		}
	}

	fun interface MiningSpeedModifier {
		fun getMiningSpeed(originalMiningSpeed: Float): Float
	}

	fun interface FoodExhaustionModifier {
		fun getExhaustion(originalExhaustion: Float): Float
	}

	@JvmRecord
	data class AwardedStat(val stat: Stat<*>, val amount: Int)
}
