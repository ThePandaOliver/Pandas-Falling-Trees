/*
 * Copyright (C) 2024 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.pandamods.fallingtrees.api;

import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stat;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public record TreeData(
		List<BlockPos> blocks,
		List<ItemStack> drops,
		List<AwardedStat> awardedStats,
		int toolDamage,
		MiningSpeedModifier miningSpeedModifier,
		FoodExhaustionModifier foodExhaustionModifier
) {
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private final List<BlockPos> blocks = new ArrayList<>();
		private final List<BlockPos> viewBlocks = Collections.unmodifiableList(blocks);
		private final List<ItemStack> drops = new ArrayList<>();
		private final List<ItemStack> viewDrops = Collections.unmodifiableList(drops);
		private final Map<Stat<?>, Integer> awardedStats = new HashMap<>();
		private int toolDamage = 0;
		private MiningSpeedModifier miningSpeedModifier = originalMiningSpeed -> originalMiningSpeed;
		private FoodExhaustionModifier foodExhaustionModifier = originalExhaustion -> originalExhaustion;

		private Builder() {}

		public Builder addBlock(BlockPos blockPos) {
			this.blocks.add(blockPos);
			return this;
		}

		public Builder addBlocks(BlockPos blockPos, BlockPos... otherBlocks) {
			this.blocks.add(blockPos);
			this.blocks.addAll(Arrays.asList(otherBlocks));
			return this;
		}
		
		public Builder addBlocks(Collection<BlockPos> blocks) {
			this.blocks.addAll(blocks);
			return this;
		}
		
		public Builder addDrop(ItemStack drop) {
			this.drops.add(drop);
			return this;
		}

		public Builder addDrops(ItemStack drop, ItemStack... otherDrops) {
			this.drops.add(drop);
			this.drops.addAll(Arrays.asList(otherDrops));
			return this;
		}
		
		public Builder addDrops(Collection<ItemStack> drops) {
			this.drops.addAll(drops);
			return this;
		}

		public Builder addAwardedStat(Stat<?> stat) {
			return this.addAwardedStat(stat, 1);
		}
		
		public Builder addAwardedStat(Stat<?> stat, int amount) {
			this.awardedStats.compute(stat, (stat1, oldAmount) -> oldAmount == null ? amount : oldAmount + amount);
			return this;
		}

		public Builder setToolDamage(int toolDamage) {
			this.toolDamage = toolDamage;
			return this;
		}

		public Builder setMiningSpeedModifier(MiningSpeedModifier miningSpeedModifier) {
			this.miningSpeedModifier = miningSpeedModifier;
			return this;
		}

		public Builder setFoodExhaustionModifier(FoodExhaustionModifier foodExhaustionModifier) {
			this.foodExhaustionModifier = foodExhaustionModifier;
			return this;
		}

		public TreeData build() {
			return new TreeData(
					viewBlocks,
					viewDrops,
					awardedStats.entrySet().stream().map(entry -> new AwardedStat(entry.getKey(), entry.getValue())).toList(),
					toolDamage,
					miningSpeedModifier,
					foodExhaustionModifier
			);
		}
	}

	public interface MiningSpeedModifier {
		float getMiningSpeed(float originalMiningSpeed);
	}

	public interface FoodExhaustionModifier {
		float getExhaustion(float originalExhaustion);
	}
	
	public record AwardedStat(Stat<?> stat, int amount) {}
}
