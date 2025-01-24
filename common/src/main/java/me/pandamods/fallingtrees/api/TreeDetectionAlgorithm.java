package me.pandamods.fallingtrees.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Set;

public abstract class TreeDetectionAlgorithm {
	public abstract Set<BlockPos> gatherTreeBlocks(Level level, BlockPos startPos);
}
