package dev.pandasystems.fallingtrees.exceptions

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

class TreeTooBigException(blockPos: BlockPos, level: Level) :
	TreeException(String.format("Tree is too big to be processed at position %s in dimension '%s'", blockPos.toShortString(), level.dimension().location()))
