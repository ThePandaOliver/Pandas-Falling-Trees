package me.pandamods.fallingtrees.exceptions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TreeTooBigException extends TreeException {
	public TreeTooBigException(BlockPos blockPos, Level level) {
		super(String.format("Tree is too big to be processed at position %s in dimension '%s'", blockPos.toShortString(), level.dimension().location()));
	}
}
