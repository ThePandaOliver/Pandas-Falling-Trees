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

package me.pandamods.fallingtrees.compat;

import dev.architectury.injectables.annotations.ExpectPlatform;
import ht.treechop.api.FellData;
import ht.treechop.common.chop.ChopUtil;
import me.pandamods.fallingtrees.api.TreeHandler;
import me.pandamods.fallingtrees.event.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class TreeChopCompat {
	public static boolean beforeFellEvent(Level level, ServerPlayer serverPlayer, BlockPos blockPos, FellData fellData) {
		return tryMakeTreeFall(blockPos, level, serverPlayer);
	}

	public static boolean tryMakeTreeFall(BlockPos blockPos, Level level, ServerPlayer player) {
		if (isCoppedLog(level.getBlockState(blockPos)))
			return tryMakeTreeFall(blockPos.above(), level, player);
		return TreeHandler.destroyTree(level, blockPos.above(), player);
	}

	public static boolean isChoppable(Level level, BlockPos blockPos) {
		if (Compat.hasTreeChop()) {
			return ChopUtil.isBlockChoppable(level, blockPos);
		}
		return false;
	}

	@ExpectPlatform
	public static boolean isCoppedLog(BlockState blockState) {
		throw new AssertionError();
	}
}