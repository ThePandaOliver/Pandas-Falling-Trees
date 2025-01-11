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
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public record TreeData(
		List<BlockPos> blocks,
		MiningSpeedModifier miningSpeedModifier,
		int toolDamage,
		float foodExhaustionMultiply,
		int awardedBlocks
) {
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private final List<BlockPos> blocks = new ArrayList<>();
		private final List<BlockPos> viewBlocks = Collections.unmodifiableList(blocks);
		private int toolDamage = 0;
		private float foodExhaustionMultiplication = 1;
		private int awardedBlocks = 0;
		private MiningSpeedModifier miningSpeedModifier = (blockState, originalMiningSpeed) -> originalMiningSpeed;

		private Builder() {}

		public Builder setMiningSpeedModifier(MiningSpeedModifier miningSpeedModifier) {
			this.miningSpeedModifier = miningSpeedModifier;
			return this;
		}

		public Builder addBlocks(Collection<BlockPos> blocks) {
			this.blocks.addAll(blocks);
			return this;
		}

		public Builder addBlock(BlockPos blockPos) {
			this.blocks.add(blockPos);
			return this;
		}

		public Builder setToolDamage(int toolDamage) {
			this.toolDamage = toolDamage;
			return this;
		}

		public Builder setFoodExhaustion(float multiply) {
			this.foodExhaustionMultiplication = multiply;
			return this;
		}

		public Builder setAwardedBlocks(int awardedBlocks) {
			this.awardedBlocks = awardedBlocks;
			return this;
		}

		public TreeData build() {
			return new TreeData(
					viewBlocks,
					miningSpeedModifier,
					toolDamage,
					foodExhaustionMultiplication,
					awardedBlocks
			);
		}
	}

	public interface MiningSpeedModifier {
		float getMiningSpeed(BlockState blockState, float originalMiningSpeed);
	}
}
